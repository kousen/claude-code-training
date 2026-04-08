from __future__ import annotations

import sqlite3
from pathlib import Path
from typing import Iterator

import pytest

from app import create_app
from config import TestingConfig
from database import USER_DB, NOTE_DB, IMAGE_DB


def _init_db(db_path: Path, schema: str) -> None:
    """Create a database with the given schema."""
    db_path.parent.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(str(db_path))
    conn.execute(schema)
    conn.commit()
    conn.close()


@pytest.fixture()
def app(tmp_path: Path) -> Iterator:
    """Create and configure a test application instance."""
    # Point database paths to temp directory
    import database

    user_db = tmp_path / "users.db"
    note_db = tmp_path / "notes.db"
    image_db = tmp_path / "images.db"
    upload_folder = tmp_path / "uploads"
    upload_folder.mkdir()

    # Monkey-patch database paths
    database.USER_DB = user_db
    database.NOTE_DB = note_db
    database.IMAGE_DB = image_db

    # Create tables
    _init_db(user_db, "CREATE TABLE users (id TEXT PRIMARY KEY, pw TEXT)")
    _init_db(note_db, "CREATE TABLE notes (user TEXT, timestamp TEXT, note TEXT, note_id TEXT PRIMARY KEY)")
    _init_db(image_db, "CREATE TABLE images (uid TEXT PRIMARY KEY, owner TEXT, name TEXT, timestamp TEXT)")

    class TempConfig(TestingConfig):
        UPLOAD_FOLDER = str(upload_folder)

    application = create_app(TempConfig)

    yield application

    # Restore original paths
    database.USER_DB = USER_DB
    database.NOTE_DB = NOTE_DB
    database.IMAGE_DB = IMAGE_DB


@pytest.fixture()
def client(app):
    """A test client for the app."""
    return app.test_client()


@pytest.fixture()
def admin_session(client):
    """A client logged in as ADMIN."""
    from database import add_user
    add_user("ADMIN", "adminpass")
    with client.session_transaction() as sess:
        sess["current_user"] = "ADMIN"
    return client
