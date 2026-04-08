from __future__ import annotations

import hashlib
import logging
import sqlite3
from contextlib import contextmanager
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterator

from werkzeug.security import check_password_hash, generate_password_hash

logger = logging.getLogger(__name__)

USER_DB = Path("database_file/users.db")
NOTE_DB = Path("database_file/notes.db")
IMAGE_DB = Path("database_file/images.db")


@contextmanager
def get_db(db_path: Path) -> Iterator[sqlite3.Connection]:
    """Context manager for database connections with automatic commit/rollback."""
    conn = sqlite3.connect(str(db_path))
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


# ── User operations ─────────────────────────────────────────────────────


def list_users() -> list[str]:
    """Return a list of all user IDs."""
    with get_db(USER_DB) as conn:
        cursor = conn.execute("SELECT id FROM users")
        return [row[0] for row in cursor.fetchall()]


def verify(user_id: str, password: str) -> bool:
    """Verify a user's password against the stored hash."""
    with get_db(USER_DB) as conn:
        cursor = conn.execute("SELECT pw FROM users WHERE id = ?", (user_id,))
        row = cursor.fetchone()
        if row is None:
            logger.warning("Login attempt for non-existent user: %s", user_id)
            return False
        return check_password_hash(row[0], password)


def add_user(user_id: str, password: str) -> None:
    """Add a new user with a securely hashed password."""
    hashed = generate_password_hash(password)
    with get_db(USER_DB) as conn:
        conn.execute("INSERT INTO users VALUES (?, ?)", (user_id.upper(), hashed))
    logger.info("User added: %s", user_id.upper())


def delete_user_from_db(user_id: str) -> None:
    """Delete a user and all their associated notes and image records."""
    with get_db(USER_DB) as conn:
        conn.execute("DELETE FROM users WHERE id = ?", (user_id,))

    with get_db(NOTE_DB) as conn:
        conn.execute("DELETE FROM notes WHERE user = ?", (user_id,))

    with get_db(IMAGE_DB) as conn:
        conn.execute("DELETE FROM images WHERE owner = ?", (user_id,))

    logger.info("User deleted: %s", user_id)


# ── Note operations ─────────────────────────────────────────────────────


def read_notes_for_user(user_id: str) -> list[tuple[str, str, str]]:
    """Read all notes for a given user. Returns list of (note_id, timestamp, note)."""
    with get_db(NOTE_DB) as conn:
        cursor = conn.execute(
            "SELECT note_id, timestamp, note FROM notes WHERE user = ?",
            (user_id.upper(),),
        )
        return cursor.fetchall()


def match_user_id_with_note_id(note_id: str) -> str | None:
    """Return the owner of a note, or None if not found."""
    with get_db(NOTE_DB) as conn:
        cursor = conn.execute(
            "SELECT user FROM notes WHERE note_id = ?", (note_id,)
        )
        row = cursor.fetchone()
        return row[0] if row else None


def write_note_into_db(user_id: str, note_to_write: str) -> None:
    """Create a new note for the given user."""
    current_timestamp = str(datetime.now(timezone.utc))
    note_id = hashlib.sha1(
        (user_id.upper() + current_timestamp).encode()
    ).hexdigest()
    with get_db(NOTE_DB) as conn:
        conn.execute(
            "INSERT INTO notes VALUES (?, ?, ?, ?)",
            (user_id.upper(), current_timestamp, note_to_write, note_id),
        )
    logger.info("Note created for user %s", user_id.upper())


def delete_note_from_db(note_id: str) -> None:
    """Delete a note by its ID."""
    with get_db(NOTE_DB) as conn:
        conn.execute("DELETE FROM notes WHERE note_id = ?", (note_id,))
    logger.info("Note deleted: %s", note_id)


# ── Image operations ────────────────────────────────────────────────────


def image_upload_record(
    uid: str, owner: str, image_name: str, timestamp: str
) -> None:
    """Record an image upload in the database."""
    with get_db(IMAGE_DB) as conn:
        conn.execute(
            "INSERT INTO images VALUES (?, ?, ?, ?)",
            (uid, owner, image_name, timestamp),
        )
    logger.info("Image recorded: %s for user %s", uid, owner)


def list_images_for_user(owner: str) -> list[tuple[str, str, str]]:
    """List all images for a user. Returns list of (uid, timestamp, name)."""
    with get_db(IMAGE_DB) as conn:
        cursor = conn.execute(
            "SELECT uid, timestamp, name FROM images WHERE owner = ?", (owner,)
        )
        return cursor.fetchall()


def match_user_id_with_image_uid(image_uid: str) -> str | None:
    """Return the owner of an image, or None if not found."""
    with get_db(IMAGE_DB) as conn:
        cursor = conn.execute(
            "SELECT owner FROM images WHERE uid = ?", (image_uid,)
        )
        row = cursor.fetchone()
        return row[0] if row else None


def delete_image_from_db(image_uid: str) -> None:
    """Delete an image record by its UID."""
    with get_db(IMAGE_DB) as conn:
        conn.execute("DELETE FROM images WHERE uid = ?", (image_uid,))
    logger.info("Image record deleted: %s", image_uid)


if __name__ == "__main__":
    print(list_users())
