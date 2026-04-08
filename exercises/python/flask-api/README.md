# Flask API Demo

A Flask web application with authentication, note-taking, image uploads, and admin user management. Built with Flask 3.x using modern Python patterns.

This project serves as a training exercise for refactoring and modernization. It was originally a Flask 1.1.2 application with several security vulnerabilities and outdated patterns, and has been modernized to follow current best practices.


## Requirements

- Python 3.10+
- Flask 3.x (managed via `pyproject.toml`)


## How to Run

1. Create and activate a virtual environment:

```bash
python -m venv .venv
source .venv/bin/activate
```

2. Install dependencies:

```bash
pip install -e .
```

3. Run the application:

```bash
python app.py
```

Or using the Flask CLI:

```bash
flask --app "app:create_app" run --debug
```

The app runs at `http://localhost:5000` by default.


## How to Run Tests

Install dev dependencies and run pytest:

```bash
pip install -e ".[dev]"
pytest
```

There are 23 tests covering authentication, routes, admin operations, and database functions. Tests use temporary SQLite databases via `tmp_path` fixtures so they do not touch production data.


## Project Structure

```
flask-api/
  app.py           # Application factory (create_app) and route definitions
  config.py        # Config / TestingConfig class hierarchy
  database.py      # Database operations with context manager (get_db)
  pyproject.toml   # Project metadata and dependencies
  database_file/   # SQLite databases (users, notes, images)
  templates/       # Jinja2 templates with Bootstrap
  static/          # CSS, JS, and static images
  tests/           # Pytest suite
    conftest.py    # Fixtures (app, client, admin_session)
    test_auth.py   # Login/logout tests
    test_routes.py # Public/private page tests
    test_admin.py  # Admin user management tests
    test_database.py # Database operation tests
```


## Architecture

**App factory pattern** -- The `create_app()` function in `app.py` accepts an optional config class, making the app testable and configurable. Tests pass `TestingConfig` to get isolated behavior.

**Config class hierarchy** -- `Config` provides base settings (secret key, upload folder, max content length). `TestingConfig` extends it with `TESTING = True` and a fixed secret key.

**Database context manager** -- `get_db()` in `database.py` provides automatic commit/rollback and connection cleanup for all SQLite operations.

**Parameterized SQL** -- All queries use `?` placeholders to prevent SQL injection (the original code used string formatting).

**Password hashing** -- Uses `werkzeug.security.generate_password_hash` / `check_password_hash` instead of the original plain SHA-256 hashing.

**HTTP method safety** -- Delete operations use POST requests instead of GET to prevent CSRF-style attacks via link clicks.


## Default Accounts

The database ships with seed accounts including `admin` (password: `admin`). Log in as admin to manage users (list, add, delete) from the Admin tab.

**Important:** If the database was seeded before the password hashing migration, existing password hashes will use the old SHA-256 format and login will fail. In that case, delete `database_file/users.db` and re-seed it with new accounts using the current `add_user()` function, which applies werkzeug's secure hashing.


## Key Pages

- **Public** (`/public/`) -- Accessible without login.
- **Private** (`/private/`) -- Requires login. Users can write notes and upload images.
- **Admin** (`/admin/`) -- Requires login as ADMIN. Manage user accounts.


## Credit

- Image `private.jpg`: https://commons.wikimedia.org/wiki/File:(315-365)_Locked_(6149414678).jpg
- Image `public.jpg`: https://commons.wikimedia.org/wiki/File:Drown%3F!_(131380682).jpg
