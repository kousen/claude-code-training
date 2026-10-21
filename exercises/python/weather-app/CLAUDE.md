# Weather App

Single-file Flask app showing current weather and a four-day forecast from the free OpenWeather APIs. It is a lab exercise for the Claude Code training course; `EXERCISE.md` holds the student tasks and `README.md` has the architecture diagrams.

## Commands

```bash
source .venv/bin/activate
pip install -r requirements-dev.txt   # runtime deps plus pytest
python main.py                        # dev server on http://127.0.0.1:5000
python -m pytest -q                   # 21 tests, no network access
```

## Key facts

- The API key is read from `OWM_API_KEY` via python-dotenv. `.env` is gitignored. If unset, the app starts, logs a warning, and every lookup lands on the error page.
- All logic is in `main.py`: three routes (`/`, `/<city>`, `/error`) and one HTTP helper, `fetch_json`, which owns the shared `requests.Session`, timeout, and `raise_for_status`.
- Each city lookup makes three API calls: geocoding, current weather, forecast. Route failures through `fetch_json` and the existing `except` clauses rather than adding per-call handling.
- Tests in `test_main.py` patch `main.SESSION.get` with a URL-keyed dispatcher and freeze the clock by patching `main.datetime`. Add new HTTP fixtures to the `api` fixture's `responses` dict instead of writing new mocks.
- Weather icons in `static/assets/` are named after OpenWeather's condition strings, lowercased. Adding a condition means adding a PNG with that name.

## Conventions

- Branch first for any exercise change. Lab-answer work stays on the course delivery branch and is not ported to `main`.
- `requirements.txt` is runtime only. Test and dev tools go in `requirements-dev.txt`.
- Keep the UI, CSS, and templates as the original author wrote them unless the task is specifically about them. The app is a teaching fixture, not a product.
