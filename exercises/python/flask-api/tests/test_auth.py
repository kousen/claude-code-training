from __future__ import annotations

from database import add_user, verify, list_users


def test_add_and_verify_user(app) -> None:
    """User password should be hashed and verifiable."""
    add_user("TESTUSER", "secret123")
    assert "TESTUSER" in list_users()
    assert verify("TESTUSER", "secret123") is True
    assert verify("TESTUSER", "wrongpass") is False


def test_verify_nonexistent_user(app) -> None:
    """Verifying a non-existent user should return False, not crash."""
    assert verify("NOBODY", "password") is False


def test_login_sets_session(client) -> None:
    add_user("ALICE", "pass123")
    client.post("/login", data={"id": "alice", "pw": "pass123"})
    with client.session_transaction() as sess:
        assert sess.get("current_user") == "ALICE"


def test_login_wrong_password(client) -> None:
    add_user("BOB", "correct")
    client.post("/login", data={"id": "bob", "pw": "wrong"})
    with client.session_transaction() as sess:
        assert "current_user" not in sess


def test_logout_clears_session(client) -> None:
    add_user("CAROL", "pass")
    with client.session_transaction() as sess:
        sess["current_user"] = "CAROL"
    client.get("/logout/")
    with client.session_transaction() as sess:
        assert "current_user" not in sess
