"""Tests for the weather app.

All OpenWeather calls go through ``requests.get``, so that single function is
mocked. Each test hands ``fake_get`` a list of canned responses in the order the
app makes calls: geocoding, current weather, forecast.
"""
import datetime
import logging
from unittest.mock import MagicMock, patch

import pytest
import requests

import main


# --- Fixtures ---------------------------------------------------------------

def assert_redirects_to(response, path):
    """Flask 3.2 changes redirect() from 302 to 303; accept either."""
    assert 300 <= response.status_code < 400, response.status_code
    assert response.headers["Location"].endswith(path), response.headers["Location"]


@pytest.fixture
def client():
    main.app.config["TESTING"] = True
    return main.app.test_client()


def fake_response(json_body, status=200):
    """Build a stand-in for requests.Response with just what main.py uses."""
    resp = MagicMock(spec=requests.Response)
    resp.status_code = status
    resp.json.return_value = json_body
    if status >= 400:
        resp.raise_for_status.side_effect = requests.HTTPError(f"{status} Client Error")
    else:
        resp.raise_for_status.return_value = None
    return resp


GEOCODE_OK = [{"name": "Paris", "lat": 48.85, "lon": 2.35, "country": "FR"}]
GEOCODE_US = [{"name": "Springfield", "lat": 39.80, "lon": -89.64, "country": "US", "state": "Illinois"}]
GEOCODE_CA = [{"name": "Toronto", "lat": 43.65, "lon": -79.38, "country": "CA", "state": "Ontario"}]

WEATHER_OK = {
    "main": {"temp": 21.4, "temp_min": 18.6, "temp_max": 24.2},
    "weather": [{"main": "Clouds"}],
    "wind": {"speed": 3.6},
}


def forecast_ok(days=5):
    """Forecast list with a noon entry for each of the next ``days`` days,
    plus a non-noon entry to prove the 12:00 filter works."""
    today = datetime.date.today()
    items = []
    for i in range(days):
        day = today + datetime.timedelta(days=i)
        items.append({"dt_txt": f"{day} 09:00:00", "main": {"temp": 10.0}, "weather": [{"main": "Rain"}]})
        items.append({"dt_txt": f"{day} 12:00:00", "main": {"temp": 20.0 + i}, "weather": [{"main": "Clear"}]})
    return {"list": items}


# --- Query normalization (US city, state) ----------------------------------

@pytest.mark.parametrize("query, expected", [
    ("Springfield, IL", "Springfield,IL,US"),
    ("springfield, illinois", "springfield,IL,US"),
    ("Portland, or ", "Portland,OR,US"),
    ("Washington, DC", "Washington,DC,US"),
    ("Toronto, CA", "Toronto,CA,US"),        # CA is also Canada: only correct as a *fallback*
    ("Springfield", None),                   # bare city
    ("Springfield, IL, US", None),           # already complete
    ("Paris, France", None),                 # non-state second field
    ("Paris, TX, FR", None),                 # three parts never rewritten
])
def test_us_state_query(query, expected):
    assert main.us_state_query(query) == expected


@patch("main.requests.get")
def test_city_state_retries_with_us_suffix_when_typed_form_is_empty(mock_get, client):
    mock_get.side_effect = [
        fake_response([]),            # 'Springfield, IL' as typed -> geocoder reads IL as a country
        fake_response(GEOCODE_US),    # retry as 'Springfield,IL,US'
        fake_response(WEATHER_OK),
        fake_response(forecast_ok()),
    ]
    r = client.get("/weather/Springfield, IL")
    assert r.status_code == 200
    queries = [c.kwargs["params"]["q"] for c in mock_get.call_args_list[:2]]
    assert queries == ["Springfield, IL", "Springfield,IL,US"]


@patch("main.requests.get")
def test_state_code_that_is_also_a_country_code_is_not_rewritten(mock_get, client):
    """'Toronto, CA' must reach the geocoder as typed and, since Canada matches, never retry as US."""
    mock_get.side_effect = [fake_response(GEOCODE_CA), fake_response(WEATHER_OK), fake_response(forecast_ok())]
    r = client.get("/weather/Toronto, CA")
    assert r.status_code == 200
    assert mock_get.call_count == 3
    assert mock_get.call_args_list[0].kwargs["params"]["q"] == "Toronto, CA"
    html = r.data.decode()
    assert "Toronto, Ontario" in html
    assert "21ºC" in html


@patch("main.requests.get")
def test_unknown_city_state_retries_once_then_errors(mock_get, client):
    mock_get.return_value = fake_response([])
    r = client.get("/weather/Nowhere, IL")
    assert_redirects_to(r, "/error")
    assert mock_get.call_count == 2   # typed form + US retry, no weather calls


# --- Units by country --------------------------------------------------------

@patch("main.requests.get")
def test_us_location_uses_imperial_units(mock_get, client):
    mock_get.side_effect = [fake_response(GEOCODE_US), fake_response(WEATHER_OK), fake_response(forecast_ok())]
    r = client.get("/weather/springfield")
    html = r.data.decode()
    for call in mock_get.call_args_list[1:]:
        assert call.args[1]["units"] == "imperial"
    assert "21ºF" in html
    assert "3.6 mph" in html
    assert "Springfield, Illinois" in html   # display name from geocoder, not raw input


@patch("main.requests.get")
def test_non_us_location_uses_metric_units(mock_get, client):
    mock_get.side_effect = [fake_response(GEOCODE_OK), fake_response(WEATHER_OK), fake_response(forecast_ok())]
    r = client.get("/weather/paris")
    html = r.data.decode()
    for call in mock_get.call_args_list[1:]:
        assert call.args[1]["units"] == "metric"
    assert "21ºC" in html
    assert "3.6 meter/sec" in html


# --- Home page ---------------------------------------------------------------

def test_home_get_renders_search_form(client):
    r = client.get("/")
    assert r.status_code == 200
    assert b'name="search"' in r.data


def test_home_post_redirects_to_weather_page(client):
    r = client.post("/", data={"search": "London"})
    assert_redirects_to(r, "/weather/London")


@patch("main.requests.get")
def test_search_with_comma_survives_redirect(mock_get, client):
    mock_get.side_effect = [fake_response([]), fake_response(GEOCODE_US), fake_response(WEATHER_OK), fake_response(forecast_ok())]
    r = client.post("/", data={"search": "Springfield, IL"}, follow_redirects=True)
    assert r.status_code == 200
    assert mock_get.call_args_list[0].kwargs["params"]["q"] == "Springfield, IL"


def test_home_post_with_empty_search_returns_home(client):
    for data in ({}, {"search": ""}, {"search": "   "}):
        r = client.post("/", data=data)
        assert_redirects_to(r, "/")


# --- Weather page: happy path ------------------------------------------------

@patch("main.requests.get")
def test_weather_page_renders_current_and_forecast(mock_get, client):
    mock_get.side_effect = [
        fake_response(GEOCODE_OK),
        fake_response(WEATHER_OK),
        fake_response(forecast_ok()),
    ]
    r = client.get("/weather/paris")
    assert r.status_code == 200
    html = r.data.decode()
    assert "<h1> Paris </h1>" in html   # heading from geocoder name, not raw 'paris'
    assert "21ºC" in html               # rounded current temp with unit
    assert "19° - 24°" in html          # min/max rounded
    assert "Clouds" in html
    # Forecast conditions render as icon filenames; template shows days 1-4 (today uses current weather)
    icons = [line for line in html.splitlines() if 'class="weather-icon"' in line]
    assert sum("clear.png" in line for line in icons) == 4   # 12:00 entries kept
    assert not any("rain.png" in line for line in icons)     # 09:00 entries filtered out
    for i in range(1, 5):
        assert f"{20 + i}º" in html              # per-day noon temps in order
    assert mock_get.call_count == 3


@patch("main.requests.get")
def test_weather_calls_use_geocoded_coords_and_timeout(mock_get, client):
    mock_get.side_effect = [
        fake_response(GEOCODE_OK),
        fake_response(WEATHER_OK),
        fake_response(forecast_ok()),
    ]
    client.get("/weather/paris")
    geo_call, weather_call, forecast_call = mock_get.call_args_list
    assert geo_call.args[0] == main.GEOCODING_API_ENDPOINT
    assert geo_call.kwargs["params"]["q"] == "paris"  # normalized, not capitalized
    assert weather_call.args[0] == main.OWM_ENDPOINT
    assert weather_call.args[1]["lat"] == 48.85
    assert weather_call.args[1]["lon"] == 2.35
    assert forecast_call.args[0] == main.OWM_FORECAST_ENDPOINT
    for call in mock_get.call_args_list:
        assert call.kwargs["timeout"] == main.API_TIMEOUT


@patch("main.requests.get")
def test_forecast_after_noon_still_renders_four_days(mock_get, client):
    """After 12:00 UTC the API omits today's noon entry; page must not crash
    and labels must come from each entry's own date, not from today+N."""
    f = forecast_ok(days=6)
    f["list"] = f["list"][2:]  # drop today's 09:00 and 12:00 entries
    mock_get.side_effect = [fake_response(GEOCODE_OK), fake_response(WEATHER_OK), fake_response(f)]
    r = client.get("/weather/paris")
    assert r.status_code == 200
    html = r.data.decode()
    tomorrow = datetime.date.today() + datetime.timedelta(days=1)
    expected_days = [(tomorrow + datetime.timedelta(days=i)).strftime("%a") for i in range(4)]
    for day, temp in zip(expected_days, range(21, 25)):
        assert f"<p> {day} </p>" in html
        assert f"{temp}º" in html
    assert "25º" not in html  # fifth future day not shown


# --- Weather page: failure paths --------------------------------------------

@patch("main.requests.get")
def test_unknown_city_redirects_to_error(mock_get, client):
    mock_get.return_value = fake_response([])  # geocoder found nothing
    r = client.get("/weather/Xyzzyqq")
    assert_redirects_to(r, "/error")
    assert mock_get.call_count == 1  # no weather calls made


@patch("main.requests.get")
def test_bad_api_key_redirects_to_api_error(mock_get, client):
    mock_get.return_value = fake_response({"cod": 401, "message": "Invalid API key"}, status=401)
    r = client.get("/weather/Paris")
    assert_redirects_to(r, "/error?reason=api")


@patch("main.requests.get")
def test_network_failure_redirects_to_api_error(mock_get, client):
    mock_get.side_effect = requests.ConnectionError("DNS failure")
    r = client.get("/weather/Paris")
    assert_redirects_to(r, "/error?reason=api")


@patch("main.requests.get")
def test_timeout_redirects_to_api_error(mock_get, client):
    mock_get.side_effect = requests.Timeout("read timed out")
    r = client.get("/weather/Paris")
    assert_redirects_to(r, "/error?reason=api")


@patch("main.requests.get")
def test_forecast_failure_after_good_geocode_redirects(mock_get, client):
    mock_get.side_effect = [
        fake_response(GEOCODE_OK),
        fake_response(WEATHER_OK),
        fake_response({"cod": "500"}, status=500),
    ]
    r = client.get("/weather/Paris")
    assert_redirects_to(r, "/error?reason=api")


@patch("main.requests.get")
def test_malformed_forecast_entry_redirects_to_api_error(mock_get, client):
    bad = {"list": [{"dt_txt": "garbage 12:00:00", "main": {"temp": 1}, "weather": [{"main": "Clear"}]}]}
    mock_get.side_effect = [fake_response(GEOCODE_OK), fake_response(WEATHER_OK), fake_response(bad)]
    r = client.get("/weather/Paris")
    assert_redirects_to(r, "/error?reason=api")


@patch("main.requests.get")
def test_weather_payload_missing_keys_redirects_to_api_error(mock_get, client):
    mock_get.side_effect = [fake_response(GEOCODE_OK), fake_response({"cod": 200}), fake_response(forecast_ok())]
    r = client.get("/weather/Paris")
    assert_redirects_to(r, "/error?reason=api")


@patch("main.requests.get")
def test_forecast_with_no_noon_entries_renders_today_only(mock_get, client):
    f = forecast_ok()
    f["list"] = [i for i in f["list"] if "12:00:00" not in i["dt_txt"]]
    mock_get.side_effect = [fake_response(GEOCODE_OK), fake_response(WEATHER_OK), fake_response(f)]
    r = client.get("/weather/Paris")
    assert r.status_code == 200
    assert r.data.decode().count('class="forecast-item"') == 1


@patch("main.requests.get")
def test_geocode_result_without_name_or_country(mock_get, client):
    mock_get.side_effect = [fake_response([{"lat": 1.0, "lon": 2.0}]), fake_response(WEATHER_OK), fake_response(forecast_ok())]
    r = client.get("/weather/somewhere")
    html = r.data.decode()
    assert "<h1> somewhere </h1>" in html          # falls back to the raw query
    assert mock_get.call_args_list[1].args[1]["units"] == "metric"


@patch("main.requests.get")
def test_api_failure_log_names_query_and_cause_but_not_key(mock_get, client, caplog):
    err = requests.HTTPError("401 Client Error: Unauthorized for url: https://x/geo?q=Paris&appid=SECRETKEY")
    err.response = MagicMock(status_code=401)
    mock_get.side_effect = err
    with caplog.at_level(logging.ERROR, logger=main.app.logger.name):
        client.get("/weather/Paris")
    assert "'Paris'" in caplog.text
    assert "HTTPError 401" in caplog.text
    assert "SECRETKEY" not in caplog.text


# --- Error page --------------------------------------------------------------

def test_error_page_default_message(client):
    r = client.get("/error")
    assert r.status_code == 200
    assert b"This city does not exist" in r.data


def test_error_page_api_message(client):
    r = client.get("/error?reason=api")
    assert r.status_code == 200
    assert b"Weather service unavailable" in r.data


# --- Routing -----------------------------------------------------------------

@patch("main.requests.get")
def test_stray_root_paths_are_404_not_api_calls(mock_get, client):
    for path in ("/favicon.ico", "/robots.txt", "/Paris"):
        assert client.get(path).status_code == 404, path
    mock_get.assert_not_called()


def test_pages_declare_favicon(client):
    for path in ("/", "/error"):
        assert b'rel="icon"' in client.get(path).data, path
