# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A single-file Flask app (`main.py`) that shows current weather and a 5-day forecast from the OpenWeather API. It is the Python exercise project for the Claude Code training course (see `EXERCISE.md` for the student tasks and the parent repo's `CLAUDE.md` for course conventions). Keep it small and readable — it exists to be explored and extended by students, not to grow features.

## Commands

Use the project venv (`.venv/`) — it has Flask, requests, pytest, and pytest-cov installed.

```bash
source .venv/bin/activate
python main.py                          # dev server on :5000, debug + auto-reload
python -m pytest                        # full suite; pytest.ini adds coverage and fails under 80%
python -m pytest test_main.py::test_us_location_uses_imperial_units   # single test
python -m pytest -k "retries or country_code"                          # by keyword (matches test names)
python -m pytest --no-cov -q            # skip the coverage gate for a quick run
python -m flask --app main run --port 5055   # if port 5000 is taken (macOS AirPlay, another instance)
```

The app needs `OWM_API_KEY` in `.env` (gitignored, already present locally). Tests do not — they mock `requests.get`.

To check behavior against the real API, hit the running server with curl; city text in the path must be URL-encoded: `curl -L 'http://127.0.0.1:5000/weather/Springfield%2C%20IL'`.

## Architecture

Three routes in `main.py`: `/` (search form, POST redirects), `/weather/<city>`, `/error`. The weather route is deliberately under a `/weather/` prefix so stray root paths (`/favicon.ico`, `/robots.txt`) 404 at the router instead of reaching the API. All links use `url_for` by endpoint name, so route paths can change without touching templates.

`get_weather` runs three sequential OpenWeather calls, all inside one `try` with a shared 10s timeout and `raise_for_status()`; any `requests.RequestException` logs the raw query and redirects to `/error?reason=api`:

1. **`geocode(query)`** — sends the input *as typed* first. Only on an empty result, and only if `us_state_query()` recognizes a `City, ST` / `City, State Name` form, does it retry as `City,ST,US`. The order matters: OpenWeather reads a 2-field query as `city,country`, and many US state codes are also ISO country codes (`CA`, `DE`, `IN`, `GA`, …). Don't "simplify" this into an unconditional rewrite — it was tried and broke `Toronto, CA`.
2. **Units** — decided by the geocoder's `country`, not the input: `US` → `units=imperial` (°F, mph), else `metric`. `UNITS` maps the API param to the two template labels; keep the three in sync through that dict.
3. **Forecast** — filtered to the `12:00:00` entries for *future* days, capped at 4, with each day label derived from the entry's own `dt_txt`. Today's noon entry vanishes from the API after 12:00 UTC, so any approach that counts labels from `today` and temps from the list will misalign in the afternoon. `city.html` shows today's current conditions in slot 0, then loops over `forecast`.

The page heading always comes from the geocoder (`name` + `state`), never from re-capitalized URL text.

Templates render weather icons as `static/assets/{{ condition.lower() }}.png` using OpenWeather's `weather[0].main` string; a new condition value needs a matching PNG.

## Testing conventions

`test_main.py` patches `main.requests.get` and feeds it an ordered list of `fake_response(...)` objects matching the call sequence: geocode (possibly twice), weather, forecast. When adding a code path that changes the number or order of HTTP calls, update `side_effect` lists accordingly — a test that fails with `StopIteration` means the app made more calls than the test supplied.

Never mutate a `params` dict between calls in `main.py`; the mock records dicts by reference, and the tests assert on what each call sent.

## Environment notes

`requirements.txt` still carries the original project's Jupyter/notebook pins; the app uses only Flask, requests, python-dotenv, gunicorn, pytest, and pytest-cov. Its pins (`Flask~=2.1.2`, `Werkzeug==2.0.2`, `requests~=2.28.0`, `python-dotenv~=0.20.0`, `gunicorn==20.1.0`) are stale relative to what `.venv/` actually has and the suite was verified against: Flask 3.0.3, Werkzeug 3.0.3, requests 2.32.3, python-dotenv 1.0.1, gunicorn 23.0.0, pytest 9.1.1, pytest-cov 7.1.0 on Python 3.11.9. A cleanup of `requirements.txt` is pending — don't edit it as a side effect of other work.

`.claude/settings.local.json` pre-approves `python`, `pip install`, `curl`, and `WebFetch` for openweathermap.org.

## Project skills and agents

- `.claude/skills/osquery/SKILL.md` — a project-scoped `osquery` skill for system diagnostics (`osqueryi --json`). It shadows the user-level skill of the same name and loads at session start, so `/osquery` here runs this copy.
- `.claude/agents/dependency-compliance-auditor.md` — a project-scoped subagent that checks dependency usage against each library's current recommendations. Useful for the pending `requirements.txt` cleanup.
