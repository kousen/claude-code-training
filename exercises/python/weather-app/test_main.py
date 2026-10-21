"""Characterization tests for the weather app.

Every route except ``/`` and ``/error`` makes three live HTTP calls through
``requests.get``.  We patch ``main.requests.get`` (patch where it's *used*)
with a dispatcher keyed on the endpoint URL, so no network is touched.
"""
import datetime
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
        resp.raise_for_status.side_effect = requests.HTTPError(f"{status} error")
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
FORECAST_OK = {"list": [
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
    """Patch requests.get; tests override entries in ``api.responses`` as needed."""
    responses = {
        main.GEOCODING_API_ENDPOINT: fake_response(GEO_OK),
        main.OWM_ENDPOINT: fake_response(WEATHER_OK),
        main.OWM_FORECAST_ENDPOINT: fake_response(FORECAST_OK),
    }

    def dispatch(url, params=None, **kwargs):
        return responses[url]

    with patch.object(main.requests, "get", side_effect=dispatch) as mock_get:
        mock_get.responses = responses
        yield mock_get


@pytest.fixture
def frozen_clock():
    with patch.object(main, "datetime", types.SimpleNamespace(datetime=FrozenDateTime)):
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
    html = resp.data.decode()

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
    assert api.call_args_list[1].args[1]["lat"] == 51.5
    assert api.call_args_list[1].args[1]["units"] == "metric"


def test_forecast_skips_today_and_non_noon_and_caps_at_four(client, api, frozen_clock):
    html = client.get("/london").data.decode()
    # Order matters: today's label first, then the next four days.
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
    html = client.get("/london").data.decode()
    for label in ("Thu", "Fri", "Sat", "Sun"):
        assert f"<p> {label} </p>" in html
    assert "<p> Mon </p>" not in html


# --------------------------------------------------------------------------- #
# Error paths (current behavior, warts included)
# --------------------------------------------------------------------------- #
def test_unknown_city_redirects_to_error_page(client, api):
    api.responses[main.GEOCODING_API_ENDPOINT] = fake_response([])
    resp = client.get("/xyzzy")
    assert resp.status_code == 302
    assert resp.headers["Location"].endswith("/error")
    assert api.call_count == 1                 # no weather calls after a miss


def test_error_page_renders(client):
    assert client.get("/error").status_code == 200


def test_geocoding_error_payload_crashes_with_keyerror(client, api):
    """A 401 comes back as a dict, which is truthy, so ``location_data[0]`` blows up."""
    api.responses[main.GEOCODING_API_ENDPOINT] = fake_response(
        {"cod": 401, "message": "Invalid API key"}, status=401
    )
    with pytest.raises(KeyError):
        client.get("/london")


def test_weather_http_error_propagates(client, api):
    """Only the current-weather call runs raise_for_status; a 500 there is unhandled."""
    api.responses[main.OWM_ENDPOINT] = fake_response({}, status=500)
    with pytest.raises(requests.HTTPError):
        client.get("/london")


def test_catch_all_route_treats_any_path_as_a_city(client, api):
    api.responses[main.GEOCODING_API_ENDPOINT] = fake_response([])
    resp = client.get("/favicon.ico")
    assert resp.status_code == 302
    assert api.call_args.kwargs["params"]["q"] == "Favicon.ico"
