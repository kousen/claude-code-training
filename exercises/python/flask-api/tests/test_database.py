from __future__ import annotations

from database import (
    add_user,
    delete_user_from_db,
    list_users,
    write_note_into_db,
    read_notes_for_user,
    delete_note_from_db,
    match_user_id_with_note_id,
)


def test_add_and_list_users(app) -> None:
    add_user("USER1", "pw1")
    add_user("USER2", "pw2")
    users = list_users()
    assert "USER1" in users
    assert "USER2" in users


def test_delete_user_cascades(app) -> None:
    """Deleting a user should also remove their notes."""
    add_user("DELME", "pw")
    write_note_into_db("DELME", "a note")
    assert len(read_notes_for_user("DELME")) == 1

    delete_user_from_db("DELME")
    assert "DELME" not in list_users()
    assert len(read_notes_for_user("DELME")) == 0


def test_write_and_read_notes(app) -> None:
    add_user("NOTER", "pw")
    write_note_into_db("NOTER", "First note")
    write_note_into_db("NOTER", "Second note")

    notes = read_notes_for_user("NOTER")
    assert len(notes) == 2
    note_texts = [n[2] for n in notes]
    assert "First note" in note_texts
    assert "Second note" in note_texts


def test_delete_note(app) -> None:
    add_user("DNOTE", "pw")
    write_note_into_db("DNOTE", "to delete")
    notes = read_notes_for_user("DNOTE")
    note_id = notes[0][0]

    delete_note_from_db(note_id)
    assert len(read_notes_for_user("DNOTE")) == 0


def test_match_user_with_note(app) -> None:
    add_user("OWNER", "pw")
    write_note_into_db("OWNER", "my note")
    notes = read_notes_for_user("OWNER")
    note_id = notes[0][0]

    assert match_user_id_with_note_id(note_id) == "OWNER"
    assert match_user_id_with_note_id("nonexistent") is None
