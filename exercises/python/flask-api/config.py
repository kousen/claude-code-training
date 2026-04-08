from __future__ import annotations

import os


class Config:
    """Base configuration."""

    SECRET_KEY: str = os.environ.get("SECRET_KEY", os.urandom(32).hex())
    UPLOAD_FOLDER: str = os.environ.get("UPLOAD_FOLDER", "image_pool")
    MAX_CONTENT_LENGTH: int = 16 * 1024 * 1024  # 16 MB


class TestingConfig(Config):
    """Configuration for tests."""

    TESTING: bool = True
    SECRET_KEY: str = "testing-secret-key-not-for-production"
