import datetime
from unittest.mock import MagicMock, patch

import pytest
import requests

import main


def fake_response(payload=None, status=200):
    response = MagicMock()
    response.json.return_value = payload
    if status >= 400:
        response.raise_for_status.side_effect = requests.HTTPError(f"{status} Error")
    return response


def forecast_entry(date, time, temp, weather="Clear"):
    return {"dt_txt": f"{date} {time}", "main": {"temp": temp}, "weather": [{"main": weather}]}


GEO = [{"lat": 51.5, "lon": -0.12}]
CURRENT = {
    "main": {"temp": 14.6, "temp_min": 11.2, "temp_max": 17.8},
    "weather": [{"main": "Clouds"}],
    "wind": {"speed": 4.1},
}


def five_day_forecast():
    """Entries at 09:00 and 12:00 for today plus the next four days, like the real 3-hour feed."""
    today = datetime.date.today()
    entries = []
    for offset in range(5):
        date = (today + datetime.timedelta(days=offset)).isoformat()
        entries.append(forecast_entry(date, "09:00:00", 0))
        entries.append(forecast_entry(date, "12:00:00", 20 + offset, "Rain"))
    return {"list": entries}


@pytest.fixture
def client():
    main.app.config["TESTING"] = True
    return main.app.test_client()


# --- noon_forecast: the day-label logic, tested without any mocking ---

def test_noon_forecast_skips_today_and_labels_from_entry_dates():
    entries = [
        forecast_entry("2026-09-23", "12:00:00", 99),  # today: excluded
        forecast_entry("2026-09-24", "09:00:00", 99),  # not noon: excluded
        forecast_entry("2026-09-24", "12:00:00", 18.4, "Rain"),
        forecast_entry("2026-09-25", "12:00:00", 19.6),
    ]
    assert main.noon_forecast(entries, "2026-09-23") == [
        {"day": "Thu", "temp": 18, "weather": "Rain"},
        {"day": "Fri", "temp": 20, "weather": "Clear"},
    ]


def test_noon_forecast_after_noon_utc_still_labels_correctly():
    # After 12:00 UTC the feed no longer contains today's noon entry; labels must still match data
    entries = [forecast_entry("2026-09-23", "15:00:00", 99)] + [
        forecast_entry(f"2026-09-{day}", "12:00:00", day) for day in range(24, 29)
    ]
    result = main.noon_forecast(entries, "2026-09-23")
    assert [d["day"] for d in result] == ["Thu", "Fri", "Sat", "Sun"]
    assert [d["temp"] for d in result] == [24, 25, 26, 27]


# --- routes ---

def test_home_page_renders(client):
    assert client.get("/").status_code == 200


def test_search_redirects_to_city_page(client):
    response = client.post("/", data={"search": "london"})
    assert response.status_code == 302
    assert response.headers["Location"] == "/london"


def test_blank_search_stays_on_home_page(client):
    response = client.post("/", data={"search": "   "})
    assert response.headers["Location"] == "/"


@patch("main.requests.get")
def test_city_page_shows_current_weather_and_forecast(mock_get, client):
    mock_get.side_effect = [fake_response(GEO), fake_response(CURRENT), fake_response(five_day_forecast())]

    response = client.get("/london")

    html = response.get_data(as_text=True)
    assert response.status_code == 200
    assert "London" in html
    assert "15ºC" in html
    assert "11° - 18°" in html
    assert html.count("rain.png") == 4  # four forecast days, today excluded
    for call in mock_get.call_args_list:
        assert call.args[0].startswith("https://")
        assert call.kwargs["timeout"] == main.REQUEST_TIMEOUT


@patch("main.requests.get")
def test_unknown_city_redirects_to_error(mock_get, client):
    mock_get.return_value = fake_response([])

    response = client.get("/notacity")

    assert response.status_code == 302
    assert response.headers["Location"] == "/error"
    assert mock_get.call_count == 1


@pytest.mark.parametrize("failing_call", [0, 1, 2])
@patch("main.requests.get")
def test_api_http_error_shows_service_error(mock_get, client, failing_call):
    responses = [fake_response(GEO), fake_response(CURRENT), fake_response(five_day_forecast())]
    responses[failing_call] = fake_response(status=401)
    mock_get.side_effect = responses

    response = client.get("/london")

    assert response.status_code == 503
    assert main.SERVICE_ERROR in response.get_data(as_text=True)


@patch("main.requests.get", side_effect=requests.Timeout("timed out"))
def test_network_timeout_shows_service_error(mock_get, client):
    response = client.get("/london")
    assert response.status_code == 503
    assert main.SERVICE_ERROR in response.get_data(as_text=True)


def test_error_page_default_message(client):
    assert "This city does not exist" in client.get("/error").get_data(as_text=True)
