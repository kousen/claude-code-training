"""Comprehensive tests for the weather-app Flask application.

Tests cover all routes, error handlers, API call chains, custom exceptions,
edge cases, and forecast graceful degradation. All external HTTP calls are
mocked via unittest.mock to avoid hitting real APIs.
"""

import datetime
from unittest.mock import MagicMock, patch

import pytest
import requests

# Patch the API key before importing the app so the module-level check passes.
with patch.dict("os.environ", {"OWM_API_KEY": "test-api-key-12345"}):
    import main  # noqa: E402


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def client():
    """Create a Flask test client with testing mode enabled."""
    main.app.config["TESTING"] = True
    with main.app.test_client() as c:
        yield c


@pytest.fixture
def ensure_api_key():
    """Ensure the module-level api_key is set for tests that need it."""
    original = main.api_key
    main.api_key = "test-api-key-12345"
    yield
    main.api_key = original


def _geo_response(lat=51.5074, lon=-0.1278):
    """Return a mock geocoding API response."""
    resp = MagicMock()
    resp.json.return_value = [{"lat": lat, "lon": lon}]
    resp.raise_for_status.return_value = None
    return resp


def _weather_response(temp=15.4, weather="Clear", temp_min=12.0, temp_max=18.0, wind=3.5):
    """Return a mock current-weather API response."""
    resp = MagicMock()
    resp.json.return_value = {
        "main": {"temp": temp, "temp_min": temp_min, "temp_max": temp_max},
        "weather": [{"main": weather}],
        "wind": {"speed": wind},
    }
    resp.raise_for_status.return_value = None
    return resp


def _forecast_response():
    """Return a mock 5-day forecast API response with noon entries."""
    items = []
    for i in range(5):
        items.append({
            "main": {"temp": 14.0 + i},
            "weather": [{"main": "Clouds"}],
            "dt_txt": f"2025-07-{10 + i} 12:00:00",
        })
        # Add a non-noon entry that should be filtered out
        items.append({
            "main": {"temp": 10.0 + i},
            "weather": [{"main": "Rain"}],
            "dt_txt": f"2025-07-{10 + i} 06:00:00",
        })
    resp = MagicMock()
    resp.json.return_value = {"list": items}
    resp.raise_for_status.return_value = None
    return resp


# ---------------------------------------------------------------------------
# Home route tests
# ---------------------------------------------------------------------------

class TestHomeRoute:
    def test_get_home_returns_200(self, client):
        rv = client.get("/")
        assert rv.status_code == 200

    def test_post_home_redirects_to_city(self, client):
        rv = client.post("/", data={"search": "London"}, follow_redirects=False)
        assert rv.status_code == 302
        assert "/London" in rv.headers["Location"]

    def test_post_home_empty_search(self, client):
        rv = client.post("/", data={"search": ""}, follow_redirects=False)
        assert rv.status_code == 302


# ---------------------------------------------------------------------------
# Error handler tests
# ---------------------------------------------------------------------------

class TestErrorHandlers:
    def test_404_page(self, client):
        rv = client.get("/nonexistent/route/here")
        assert rv.status_code == 404
        assert b"Page not found" in rv.data

    def test_error_route(self, client):
        rv = client.get("/error")
        assert rv.status_code == 200


# ---------------------------------------------------------------------------
# Weather route — happy path
# ---------------------------------------------------------------------------

class TestWeatherHappyPath:
    @patch("main.requests.get")
    def test_full_weather_page(self, mock_get, client, ensure_api_key):
        mock_get.side_effect = [
            _geo_response(),
            _weather_response(),
            _forecast_response(),
        ]

        rv = client.get("/london")
        assert rv.status_code == 200
        assert b"London" in rv.data
        assert mock_get.call_count == 3

    @patch("main.requests.get")
    def test_city_name_capitalised(self, mock_get, client, ensure_api_key):
        mock_get.side_effect = [
            _geo_response(),
            _weather_response(),
            _forecast_response(),
        ]

        rv = client.get("/new york")
        assert rv.status_code == 200
        assert b"New York" in rv.data

    @patch("main.requests.get")
    def test_temperature_rounding(self, mock_get, client, ensure_api_key):
        mock_get.side_effect = [
            _geo_response(),
            _weather_response(temp=15.7, temp_min=12.3, temp_max=18.9),
            _forecast_response(),
        ]

        rv = client.get("/london")
        assert rv.status_code == 200
        # 15.7 rounds to 16, 12.3 to 12, 18.9 to 19
        assert b"16" in rv.data


# ---------------------------------------------------------------------------
# API key missing
# ---------------------------------------------------------------------------

class TestAPIKeyMissing:
    def test_raises_error_when_api_key_not_set(self, client):
        original = main.api_key
        main.api_key = None
        try:
            rv = client.get("/london")
            assert b"not configured" in rv.data or b"OWM_API_KEY" in rv.data
        finally:
            main.api_key = original


# ---------------------------------------------------------------------------
# Geocoding errors
# ---------------------------------------------------------------------------

class TestGeocodingErrors:
    @patch("main.requests.get")
    def test_geocoding_timeout(self, mock_get, client, ensure_api_key):
        mock_get.side_effect = requests.exceptions.Timeout("timed out")
        rv = client.get("/london")
        assert b"timed out" in rv.data

    @patch("main.requests.get")
    def test_geocoding_connection_error(self, mock_get, client, ensure_api_key):
        mock_get.side_effect = requests.exceptions.ConnectionError("no connection")
        rv = client.get("/london")
        assert b"internet connection" in rv.data

    @patch("main.requests.get")
    def test_geocoding_http_error(self, mock_get, client, ensure_api_key):
        resp = MagicMock()
        resp.status_code = 500
        resp.raise_for_status.side_effect = requests.exceptions.HTTPError(response=resp)
        mock_get.return_value = resp
        rv = client.get("/london")
        assert b"geocoding service returned an error" in rv.data

    @patch("main.requests.get")
    def test_geocoding_empty_results(self, mock_get, client, ensure_api_key):
        resp = MagicMock()
        resp.json.return_value = []
        resp.raise_for_status.return_value = None
        mock_get.return_value = resp

        rv = client.get("/xyznonexistent")
        assert b"not found" in rv.data

    @patch("main.requests.get")
    def test_geocoding_malformed_response_missing_lat(self, mock_get, client, ensure_api_key):
        resp = MagicMock()
        resp.json.return_value = [{"name": "London"}]  # missing lat/lon
        resp.raise_for_status.return_value = None
        mock_get.return_value = resp

        rv = client.get("/london")
        assert b"unexpected data" in rv.data

    @patch("main.requests.get")
    def test_geocoding_key_error_in_json_parse(self, mock_get, client, ensure_api_key):
        resp = MagicMock()
        resp.json.side_effect = ValueError("bad json")
        resp.raise_for_status.return_value = None
        mock_get.return_value = resp

        rv = client.get("/london")
        assert b"unexpected response" in rv.data


# ---------------------------------------------------------------------------
# Current weather errors
# ---------------------------------------------------------------------------

class TestWeatherErrors:
    @patch("main.requests.get")
    def test_weather_timeout(self, mock_get, client, ensure_api_key):
        mock_get.side_effect = [
            _geo_response(),
            requests.exceptions.Timeout("weather timed out"),
        ]
        rv = client.get("/london")
        assert b"weather service timed out" in rv.data

    @patch("main.requests.get")
    def test_weather_connection_error(self, mock_get, client, ensure_api_key):
        mock_get.side_effect = [
            _geo_response(),
            requests.exceptions.ConnectionError("no connection"),
        ]
        rv = client.get("/london")
        assert b"internet connection" in rv.data

    @patch("main.requests.get")
    def test_weather_http_error(self, mock_get, client, ensure_api_key):
        resp = MagicMock()
        resp.status_code = 503
        resp.raise_for_status.side_effect = requests.exceptions.HTTPError(response=resp)

        mock_get.side_effect = [_geo_response(), resp]
        rv = client.get("/london")
        assert b"weather service returned an error" in rv.data

    @patch("main.requests.get")
    def test_weather_malformed_response(self, mock_get, client, ensure_api_key):
        resp = MagicMock()
        resp.json.return_value = {"main": {}}  # missing keys
        resp.raise_for_status.return_value = None

        mock_get.side_effect = [_geo_response(), resp]
        rv = client.get("/london")
        assert b"unexpected data" in rv.data

    @patch("main.requests.get")
    def test_weather_missing_weather_key(self, mock_get, client, ensure_api_key):
        resp = MagicMock()
        resp.json.return_value = {
            "main": {"temp": 15, "temp_min": 12, "temp_max": 18},
            "weather": [],  # empty list triggers IndexError
            "wind": {"speed": 3},
        }
        resp.raise_for_status.return_value = None

        mock_get.side_effect = [_geo_response(), resp]
        rv = client.get("/london")
        assert b"unexpected data" in rv.data


# ---------------------------------------------------------------------------
# Forecast graceful degradation
# ---------------------------------------------------------------------------

class TestForecastDegradation:
    @patch("main.requests.get")
    def test_forecast_timeout_still_shows_weather(self, mock_get, client, ensure_api_key):
        forecast_resp = MagicMock()
        forecast_resp.raise_for_status.side_effect = requests.exceptions.Timeout()

        mock_get.side_effect = [
            _geo_response(),
            _weather_response(),
            forecast_resp,
        ]

        rv = client.get("/london")
        assert rv.status_code == 200
        assert b"London" in rv.data

    @patch("main.requests.get")
    def test_forecast_connection_error_still_shows_weather(self, mock_get, client, ensure_api_key):
        forecast_resp = MagicMock()
        forecast_resp.raise_for_status.side_effect = requests.exceptions.ConnectionError()

        mock_get.side_effect = [
            _geo_response(),
            _weather_response(),
            forecast_resp,
        ]

        rv = client.get("/london")
        assert rv.status_code == 200
        assert b"London" in rv.data

    @patch("main.requests.get")
    def test_forecast_malformed_data_still_shows_weather(self, mock_get, client, ensure_api_key):
        forecast_resp = MagicMock()
        forecast_resp.json.return_value = {}  # missing "list" key
        forecast_resp.raise_for_status.return_value = None

        mock_get.side_effect = [
            _geo_response(),
            _weather_response(),
            forecast_resp,
        ]

        rv = client.get("/london")
        assert rv.status_code == 200
        assert b"London" in rv.data

    @patch("main.requests.get")
    def test_forecast_http_error_still_shows_weather(self, mock_get, client, ensure_api_key):
        forecast_resp = MagicMock()
        forecast_resp.raise_for_status.side_effect = requests.exceptions.HTTPError(
            response=MagicMock(status_code=500)
        )

        mock_get.side_effect = [
            _geo_response(),
            _weather_response(),
            forecast_resp,
        ]

        rv = client.get("/london")
        assert rv.status_code == 200
        assert b"London" in rv.data


# ---------------------------------------------------------------------------
# Custom exception hierarchy
# ---------------------------------------------------------------------------

class TestCustomExceptions:
    def test_city_not_found_is_weather_app_error(self):
        assert issubclass(main.CityNotFoundError, main.WeatherAppError)

    def test_weather_api_error_is_weather_app_error(self):
        assert issubclass(main.WeatherAPIError, main.WeatherAppError)

    def test_api_key_missing_is_weather_app_error(self):
        assert issubclass(main.APIKeyMissingError, main.WeatherAppError)

    def test_weather_app_error_is_exception(self):
        assert issubclass(main.WeatherAppError, Exception)


# ---------------------------------------------------------------------------
# API call parameters
# ---------------------------------------------------------------------------

class TestAPICallParameters:
    @patch("main.requests.get")
    def test_geocoding_called_with_correct_params(self, mock_get, client, ensure_api_key):
        mock_get.side_effect = [
            _geo_response(),
            _weather_response(),
            _forecast_response(),
        ]

        client.get("/paris")

        geo_call = mock_get.call_args_list[0]
        assert geo_call[0][0] == main.GEOCODING_API_ENDPOINT
        assert geo_call[1]["params"]["q"] == "Paris"
        assert geo_call[1]["params"]["appid"] == "test-api-key-12345"
        assert geo_call[1]["params"]["limit"] == 3
        assert geo_call[1]["timeout"] == main.API_TIMEOUT

    @patch("main.requests.get")
    def test_weather_called_with_geocoded_coords(self, mock_get, client, ensure_api_key):
        mock_get.side_effect = [
            _geo_response(lat=48.8566, lon=2.3522),
            _weather_response(),
            _forecast_response(),
        ]

        client.get("/paris")

        weather_call = mock_get.call_args_list[1]
        assert weather_call[0][0] == main.OWM_ENDPOINT
        assert weather_call[1]["params"]["lat"] == 48.8566
        assert weather_call[1]["params"]["lon"] == 2.3522
        assert weather_call[1]["params"]["units"] == "metric"

    @patch("main.requests.get")
    def test_forecast_called_with_same_coords(self, mock_get, client, ensure_api_key):
        mock_get.side_effect = [
            _geo_response(lat=48.8566, lon=2.3522),
            _weather_response(),
            _forecast_response(),
        ]

        client.get("/paris")

        forecast_call = mock_get.call_args_list[2]
        assert forecast_call[0][0] == main.OWM_FORECAST_ENDPOINT
        assert forecast_call[1]["params"]["lat"] == 48.8566
        assert forecast_call[1]["params"]["lon"] == 2.3522


# ---------------------------------------------------------------------------
# WeatherAppError handler
# ---------------------------------------------------------------------------

class TestWeatherAppErrorHandler:
    @patch("main.requests.get")
    def test_weather_app_error_renders_error_template(self, mock_get, client, ensure_api_key):
        mock_get.side_effect = requests.exceptions.Timeout("timed out")
        rv = client.get("/london")
        # The WeatherAppError handler should render error.html
        # (not return a 500 — it's a handled application error)
        assert rv.status_code == 200

    def test_api_key_missing_renders_error_template(self, client):
        original = main.api_key
        main.api_key = None
        try:
            rv = client.get("/london")
            assert rv.status_code == 200
            assert b"OWM_API_KEY" in rv.data
        finally:
            main.api_key = original
