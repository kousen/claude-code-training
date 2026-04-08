from __future__ import annotations

from database import add_user, list_users


def test_admin_page_accessible(admin_session) -> None:
    response = admin_session.get("/admin/")
    assert response.status_code == 200


def test_add_user_via_admin(admin_session) -> None:
    response = admin_session.post("/add_user", data={"id": "newuser", "pw": "pass123"})
    assert response.status_code == 302
    assert "NEWUSER" in list_users()


def test_add_duplicate_user(admin_session) -> None:
    add_user("DUPE", "pw")
    response = admin_session.post("/add_user", data={"id": "dupe", "pw": "pw2"})
    assert response.status_code == 200  # re-renders admin page with warning
    assert b"already exists" in response.data


def test_add_user_with_invalid_chars(admin_session) -> None:
    response = admin_session.post("/add_user", data={"id": "bad user", "pw": "pw"})
    assert response.status_code == 200
    assert b"invalid" in response.data


def test_delete_user_via_admin(admin_session) -> None:
    add_user("VICTIM", "pw")
    response = admin_session.post("/delete_user/VICTIM/")
    assert response.status_code == 302
    assert "VICTIM" not in list_users()


def test_cannot_delete_admin(admin_session) -> None:
    response = admin_session.post("/delete_user/ADMIN/")
    assert response.status_code == 403
