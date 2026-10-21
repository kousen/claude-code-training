"""Characterization tests for the weather app.

The ``/<city>`` route makes three live HTTP calls through ``main.SESSION.get``.
We patch that attribute (patch where it's *used*) with a dispatcher keyed on
the endpoint URL, so no network is touched.
"""
import datetime
import logging
import types
from unittest.mock import Mock, patch

import pytest
import requests

import main

FROZEN_NOW = datetime.datetime(2026, 10, 21, 10, 0)  # Wednesday, 10:00 local


# --------------------------------------------------------------------------- #
# Fakes
# --------------------------------------------------------------------------- #
def fake_response(payload, status=200):
    resp = Mock()
    resp.status_code = status
    resp.json.return_value = payload
    if status >= 400:
        resp.raise_for_status.side_effect = requests.HTTPError(f"{status} error", response=resp)
    else:
        resp.raise_for_status.return_value = None
    return resp


def noon(day, temp=20, weather="Clouds"):
    return {"dt_txt": f"2026-10-{day:02d} 12:00:00",
            "main": {"temp": temp}, "weather": [{"main": weather}]}


GEO_OK = [{"name": "London", "lat": 51.5, "lon": -0.12}]
WEATHER_OK = {
    "main": {"temp": 14.6, "temp_min": 11.2, "temp_max": 16.8},
    "weather": [{"main": "Rain"}],
    "wind": {"speed": 3.4},
}
FORECAST_OK: dict[str, list[dict]] = {"list": [
    noon(21, temp=15, weather="Clear"),                     # today -> skipped
    {"dt_txt": "2026-10-22 09:00:00",                        # not noon -> skipped
     "main": {"temp": 9}, "weather": [{"main": "Fog"}]},
    noon(22, temp=17, weather="Clouds"),
    noon(23, temp=18, weather="Rain"),
    noon(24, temp=12, weather="Snow"),
    noon(25, temp=21, weather="Clear"),
    noon(26, temp=25, weather="Clear"),                      # fifth day -> cut by [:4]
]}


class FrozenDateTime(datetime.datetime):
    @classmethod
    def now(cls, tz=None):
        return cls.fromtimestamp(FROZEN_NOW.timestamp(), tz) if tz else cls(*FROZEN_NOW.timetuple()[:6])


# --------------------------------------------------------------------------- #
# Fixtures
# --------------------------------------------------------------------------- #
@pytest.fixture
def client():
    main.app.config["TESTING"] = True  # exceptions propagate instead of 500 pages
    return main.app.test_client()


@pytest.fixture
def api():
    """Patch ``main.SESSION.get``; tests override entries in ``api.responses`` as needed."""
    responses = {
        main.GEOCODING_API_ENDPOINT: fake_response(GEO_OK),
        main.OWM_ENDPOINT: fake_response(WEATHER_OK),
        main.OWM_FORECAST_ENDPOINT: fake_response(FORECAST_OK),
    }

    def dispatch(url, **_):
        # AssertionError is not in main's caught tuple, so a wrong URL fails loudly
        assert url in responses, f"unexpected URL {url}"
        return responses[url]

    with patch("main.SESSION.get", side_effect=dispatch) as mock_get:
        mock_get.responses = responses
        yield mock_get


@pytest.fixture
def frozen_clock():
    # Keep every other attribute (timedelta, date, ...) so only the clock changes.
    fake_module = types.SimpleNamespace(**{**vars(datetime), "datetime": FrozenDateTime})
    with patch("main.datetime", fake_module):
        yield


# --------------------------------------------------------------------------- #
# Home
# --------------------------------------------------------------------------- #
def test_home_get_renders_search_form(client):
    resp = client.get("/")
    assert resp.status_code == 200
    assert b'name="search"' in resp.data


def test_home_post_redirects_to_city_route(client):
    resp = client.post("/", data={"search": "London"})
    assert resp.status_code == 302
    assert resp.headers["Location"].endswith("/London")


# --------------------------------------------------------------------------- #
# City page
# --------------------------------------------------------------------------- #
def test_city_page_happy_path(client, api, frozen_clock):
    resp = client.get("/london")
    html = resp.get_data(as_text=True)

    assert resp.status_code == 200
    assert "London" in html                    # string.capwords
    assert "Wednesday, October 21" in html
    assert "15ºC" in html                      # round(14.6)
    assert "11° - 17°" in html                 # round(11.2), round(16.8)
    assert "3.4 meter/sec" in html
    assert "Rain" in html

    geo_call = api.call_args_list[0]
    assert geo_call.args[0] == main.GEOCODING_API_ENDPOINT
    assert geo_call.kwargs["params"]["q"] == "London"
    assert api.call_args_list[1].kwargs["params"]["lat"] == 51.5
    assert api.call_args_list[1].kwargs["params"]["units"] == "metric"
    assert all(c.kwargs["timeout"] == main.REQUEST_TIMEOUT for c in api.call_args_list)


def test_forecast_skips_today_and_non_noon_and_caps_at_four(client, api, frozen_clock):
    html = client.get("/london").get_data(as_text=True)
    # Today's label (from the current-weather block) plus the next four days.
    for label in ("Wed", "Thu", "Fri", "Sat", "Sun"):
        assert f"<p> {label} </p>" in html
    assert "<p> Mon </p>" not in html          # 26th cut by [:4]
    assert "snow.png" in html                  # day data came from each entry's own date
    assert "fog.png" not in html               # 09:00 slot ignored


def test_forecast_when_todays_noon_entry_is_absent(client, api, frozen_clock):
    """After 12:00 UTC OpenWeather drops today's noon slot; labels must not shift."""
    api.responses[main.OWM_FORECAST_ENDPOINT] = fake_response(
        {"list": [e for e in FORECAST_OK["list"] if not e["dt_txt"].startswith("2026-10-21")]}
    )
    html = client.get("/london").get_data(as_text=True)
    for label in ("Thu", "Fri", "Sat", "Sun"):
        assert f"<p> {label} </p>" in html
    assert "<p> Mon </p>" not in html


# --------------------------------------------------------------------------- #
# Error paths
# --------------------------------------------------------------------------- #
def test_unknown_city_redirects_to_error_page(client, api):
    api.responses[main.GEOCODING_API_ENDPOINT] = fake_response([])
    resp = client.get("/xyzzy")
    assert resp.status_code == 302
    assert resp.headers["Location"].endswith("/error")
    assert api.call_count == 1                 # no weather calls after a miss


def test_error_page_default_message(client):
    resp = client.get("/error")
    assert resp.status_code == 200
    assert b"This city does not exist" in resp.data


def test_error_page_unavailable_message(client):
    resp = client.get("/error?reason=unavailable")
    assert resp.status_code == 200
    assert b"weather service is unavailable" in resp.data


def assert_redirects_to_unavailable(resp):
    assert resp.status_code == 302
    assert resp.headers["Location"].endswith("/error?reason=unavailable")


def test_geocoding_401_redirects_with_auth_reason(client, api):
    """A 401 on geocoding used to surface as a KeyError on the dict payload."""
    api.responses[main.GEOCODING_API_ENDPOINT] = fake_response(
        {"cod": 401, "message": "Invalid API key"}, status=401
    )
    resp = client.get("/london")
    assert resp.status_code == 302
    assert resp.headers["Location"].endswith("/error?reason=auth")
    assert api.call_count == 1


def test_error_page_auth_message(client):
    resp = client.get("/error?reason=auth")
    assert b"rejected this app" in resp.data
    assert b"OWM_API_KEY" in resp.data


def test_malformed_weather_payload_redirects(client, api):
    """200 OK with an empty ``weather`` list used to escape the try block as a 500."""
    api.responses[main.OWM_ENDPOINT] = fake_response({**WEATHER_OK, "weather": []})
    assert_redirects_to_unavailable(client.get("/london"))


def test_weather_http_error_redirects(client, api):
    api.responses[main.OWM_ENDPOINT] = fake_response({}, status=500)
    assert_redirects_to_unavailable(client.get("/london"))


def test_forecast_http_error_redirects(client, api):
    """Forecast used to skip raise_for_status entirely and fail later on a missing key."""
    api.responses[main.OWM_FORECAST_ENDPOINT] = fake_response({}, status=502)
    assert_redirects_to_unavailable(client.get("/london"))


def test_timeout_redirects(client, api):
    api.side_effect = requests.Timeout("read timed out")
    assert_redirects_to_unavailable(client.get("/london"))


def test_connection_error_redirects(client, api):
    api.side_effect = requests.ConnectionError("dns failure")
    assert_redirects_to_unavailable(client.get("/london"))


def test_catch_all_route_treats_any_path_as_a_city(client, api):
    api.responses[main.GEOCODING_API_ENDPOINT] = fake_response([])
    resp = client.get("/robots.txt")
    assert resp.status_code == 302
    assert api.call_args.kwargs["params"]["q"] == "Robots.txt"


def test_favicon_does_not_hit_the_api(client, api):
    assert client.get("/favicon.ico").status_code == 404
    assert api.call_count == 0


@pytest.mark.parametrize("form", [{}, {"search": ""}, {"search": "   "}])
def test_home_post_without_a_city_rerenders_form(client, api, form):
    """Used to raise BuildError (500) when the field was missing."""
    resp = client.post("/", data=form)
    assert resp.status_code == 200
    assert b'name="search"' in resp.data
    assert api.call_count == 0


def test_upstream_errors_never_log_the_api_key(client, api, caplog):
    """requests embeds the full URL (appid included) in its exception text."""
    leaky = "500 Server Error for url: https://x/?q=London&appid=SECRET-KEY-123"
    resp = Mock(status_code=500)
    resp.raise_for_status.side_effect = requests.HTTPError(leaky, response=resp)
    api.responses[main.OWM_ENDPOINT] = resp
    api.responses[main.OWM_FORECAST_ENDPOINT] = resp  # unreachable, but keep the dict complete

    with caplog.at_level(logging.ERROR, logger=main.app.logger.name):
        assert_redirects_to_unavailable(client.get("/london"))
    assert "SECRET-KEY-123" not in caplog.text
    assert "HTTP 500" in caplog.text

    api.side_effect = requests.ConnectionError(f"Max retries exceeded with url: /?appid=SECRET-KEY-123")
    with caplog.at_level(logging.ERROR, logger=main.app.logger.name):
        assert_redirects_to_unavailable(client.get("/london"))
    assert "SECRET-KEY-123" not in caplog.text
    assert "ConnectionError" in caplog.text
