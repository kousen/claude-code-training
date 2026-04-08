from __future__ import annotations


def test_index_returns_200(client) -> None:
    response = client.get("/")
    assert response.status_code == 200


def test_public_returns_200(client) -> None:
    response = client.get("/public/")
    assert response.status_code == 200


def test_private_requires_login(client) -> None:
    response = client.get("/private/")
    assert response.status_code == 401


def test_admin_requires_admin(client) -> None:
    response = client.get("/admin/")
    assert response.status_code == 401


def test_delete_note_uses_post(client) -> None:
    """DELETE operations must not be accessible via GET."""
    response = client.get("/delete_note/fake-id")
    assert response.status_code == 405


def test_delete_image_uses_post(client) -> None:
    """DELETE operations must not be accessible via GET."""
    response = client.get("/delete_image/fake-uid")
    assert response.status_code == 405


def test_delete_user_uses_post(client) -> None:
    """DELETE operations must not be accessible via GET."""
    response = client.get("/delete_user/TESTUSER/")
    assert response.status_code == 405
