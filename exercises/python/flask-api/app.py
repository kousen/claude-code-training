from __future__ import annotations

import hashlib
import logging
import os
from datetime import datetime, timezone
from pathlib import Path

from flask import (
    Flask,
    abort,
    flash,
    redirect,
    render_template,
    request,
    session,
    url_for,
)
from werkzeug.utils import secure_filename

from config import Config
from database import (
    add_user,
    delete_image_from_db,
    delete_note_from_db,
    delete_user_from_db,
    image_upload_record,
    list_images_for_user,
    list_users,
    match_user_id_with_image_uid,
    match_user_id_with_note_id,
    read_notes_for_user,
    verify,
)

logger = logging.getLogger(__name__)

ALLOWED_EXTENSIONS = {"png", "jpg", "jpeg", "gif"}


def allowed_file(filename: str) -> bool:
    """Check if a filename has an allowed image extension."""
    return "." in filename and filename.rsplit(".", 1)[1].lower() in ALLOWED_EXTENSIONS


def create_app(config: type | None = None) -> Flask:
    """Application factory for the Flask app."""
    app = Flask(__name__)
    app.config.from_object(config or Config)

    # ── Logging setup ───────────────────────────────────────────────────
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    )

    # ── Error handlers ──────────────────────────────────────────────────

    @app.errorhandler(401)
    def handle_401(error: Exception) -> tuple[str, int]:
        return render_template("page_401.html"), 401

    @app.errorhandler(403)
    def handle_403(error: Exception) -> tuple[str, int]:
        return render_template("page_403.html"), 403

    @app.errorhandler(404)
    def handle_404(error: Exception) -> tuple[str, int]:
        return render_template("page_404.html"), 404

    @app.errorhandler(405)
    def handle_405(error: Exception) -> tuple[str, int]:
        return render_template("page_405.html"), 405

    @app.errorhandler(413)
    def handle_413(error: Exception) -> tuple[str, int]:
        return render_template("page_413.html"), 413

    # ── Public routes ───────────────────────────────────────────────────

    @app.route("/")
    def index() -> str:
        return render_template("index.html")

    @app.route("/public/")
    def public() -> str:
        return render_template("public_page.html")

    # ── Authentication routes ───────────────────────────────────────────

    @app.route("/login", methods=["POST"])
    def login() -> str:
        id_submitted = request.form.get("id", "").upper()
        password = request.form.get("pw", "")
        if id_submitted in list_users() and verify(id_submitted, password):
            session["current_user"] = id_submitted
            logger.info("User logged in: %s", id_submitted)
        else:
            logger.warning("Failed login attempt for: %s", id_submitted)
        return redirect(url_for("index"))

    @app.route("/logout/")
    def logout() -> str:
        user = session.pop("current_user", None)
        if user:
            logger.info("User logged out: %s", user)
        return redirect(url_for("index"))

    # ── Private page ────────────────────────────────────────────────────

    @app.route("/private/")
    def private() -> str:
        if "current_user" not in session:
            abort(401)

        current_user = session["current_user"]
        notes_list = read_notes_for_user(current_user)
        notes_table = [
            (note_id, timestamp, note, f"/delete_note/{note_id}")
            for note_id, timestamp, note in notes_list
        ]

        images_list = list_images_for_user(current_user)
        images_table = [
            (uid, timestamp, name, f"/delete_image/{uid}")
            for uid, timestamp, name in images_list
        ]

        return render_template(
            "private_page.html", notes=notes_table, images=images_table
        )

    # ── Note operations ─────────────────────────────────────────────────

    @app.route("/write_note", methods=["POST"])
    def write_note() -> str:
        if "current_user" not in session:
            abort(401)
        text_to_write = request.form.get("text_note_to_take", "")
        write_note_into_db(session["current_user"], text_to_write)
        return redirect(url_for("private"))

    @app.route("/delete_note/<note_id>", methods=["POST"])
    def delete_note(note_id: str) -> str:
        current_user = session.get("current_user")
        if current_user is None or current_user != match_user_id_with_note_id(note_id):
            abort(401)
        delete_note_from_db(note_id)
        return redirect(url_for("private"))

    # ── Image operations ────────────────────────────────────────────────

    @app.route("/upload_image", methods=["POST"])
    def upload_image() -> str:
        if "current_user" not in session:
            abort(401)

        if "file" not in request.files:
            flash("No file part", category="danger")
            return redirect(url_for("private"))

        file = request.files["file"]
        if file.filename == "":
            flash("No selected file", category="danger")
            return redirect(url_for("private"))

        if file and file.filename and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            upload_time = str(datetime.now(timezone.utc))
            image_uid = hashlib.sha1(
                (upload_time + filename).encode()
            ).hexdigest()

            upload_path = Path(app.config["UPLOAD_FOLDER"])
            file.save(str(upload_path / f"{image_uid}-{filename}"))

            image_upload_record(
                image_uid, session["current_user"], filename, upload_time
            )

        return redirect(url_for("private"))

    @app.route("/delete_image/<image_uid>", methods=["POST"])
    def delete_image(image_uid: str) -> str:
        current_user = session.get("current_user")
        if current_user is None or current_user != match_user_id_with_image_uid(image_uid):
            abort(401)

        delete_image_from_db(image_uid)

        upload_folder = Path(app.config["UPLOAD_FOLDER"])
        for filepath in upload_folder.iterdir():
            if filepath.name.split("-", 1)[0] == image_uid:
                filepath.unlink()
                break

        return redirect(url_for("private"))

    # ── Admin routes ────────────────────────────────────────────────────

    @app.route("/admin/")
    def admin() -> str:
        if session.get("current_user") != "ADMIN":
            abort(401)

        user_list = list_users()
        user_table = [
            (i, user_id, f"/delete_user/{user_id}")
            for i, user_id in enumerate(user_list, 1)
        ]
        return render_template("admin.html", users=user_table)

    @app.route("/delete_user/<user_id>/", methods=["POST"])
    def delete_user(user_id: str) -> str:
        if session.get("current_user") != "ADMIN":
            abort(401)
        if user_id == "ADMIN":
            abort(403)

        # Delete user's images from the file system
        images_to_remove = list_images_for_user(user_id)
        upload_folder = Path(app.config["UPLOAD_FOLDER"])
        for image_uid, *_ in images_to_remove:
            for filepath in upload_folder.iterdir():
                if filepath.name.split("-", 1)[0] == image_uid:
                    filepath.unlink()
                    break

        delete_user_from_db(user_id)
        logger.info("Admin deleted user: %s", user_id)
        return redirect(url_for("admin"))

    @app.route("/add_user", methods=["POST"])
    def add_user_route() -> str:
        if session.get("current_user") != "ADMIN":
            abort(401)

        new_id = request.form.get("id", "").upper()
        new_pw = request.form.get("pw", "")

        if new_id in list_users():
            user_list = list_users()
            user_table = [
                (i, uid, f"/delete_user/{uid}")
                for i, uid in enumerate(user_list, 1)
            ]
            return render_template(
                "admin.html", id_to_add_is_duplicated=True, users=user_table
            )

        if " " in new_id or "'" in new_id:
            user_list = list_users()
            user_table = [
                (i, uid, f"/delete_user/{uid}")
                for i, uid in enumerate(user_list, 1)
            ]
            return render_template(
                "admin.html", id_to_add_is_invalid=True, users=user_table
            )

        add_user(new_id, new_pw)
        return redirect(url_for("admin"))

    return app


if __name__ == "__main__":
    application = create_app()
    application.run(debug=True, host="0.0.0.0")
