"""Tests for the weather app. All OpenWeather calls are mocked; no network needed."""
import datetime
import os
from unittest.mock import Mock, patch

import pytest

os.environ.setdefault("OWM_API_KEY", "test-key")

import main  # noqa: E402


def today():
    return datetime.datetime.now().date()


def forecast_entries():
    """09:00 and 12:00 entries for today and the next five days.

    Noon temps: today 99, then 21..25. All 09:00 entries are 55.
    """
    entries = []
    noon_temps = [99, 21, 22, 23, 24, 25]
    for offset, noon_temp in enumerate(noon_temps):
        day = today() + datetime.timedelta(days=offset)
        for hour, temp in (("09:00:00", 55), ("12:00:00", noon_temp)):
            entries.append({
                "dt_txt": f"{day} {hour}",
                "main": {"temp": temp},
                "weather": [{"main": "Rain"}],
            })
    return entries


def fake_owm(geo=None, weather_status=None):
    """Build a stand-in for requests.get that answers by URL."""
    payloads = {
        "geo/1.0/direct": [{"lat": 51.5, "lon": -0.12}] if geo is None else geo,
        "data/2.5/weather": {
            "main": {"temp": 18.4, "temp_min": 15.2, "temp_max": 20.6},
            "weather": [{"main": "Clouds"}],
            "wind": {"speed": 4.1},
            "timezone": 0,
        },
        "data/2.5/forecast": {"list": forecast_entries(), "city": {"timezone": 0}},
    }

    def get(url, *args, **kwargs):
        response = Mock()
        response.json.return_value = next(v for k, v in payloads.items() if k in url)
        return response

    return get


@pytest.fixture
def client():
    main.app.config["TESTING"] = True
    return main.app.test_client()


def test_home_page_shows_search_form(client):
    response = client.get("/")
    assert response.status_code == 200
    assert b'name="search"' in response.data


def test_search_redirects_to_city_page(client):
    response = client.post("/", data={"search": "london"})
    assert response.status_code == 302
    assert response.headers["Location"].endswith("/london")


def test_city_page_shows_current_weather(client):
    with patch("requests.get", side_effect=fake_owm()):
        response = client.get("/london")
    page = response.get_data(as_text=True)
    assert response.status_code == 200
    assert "London" in page
    assert "18ºC" in page
    assert "15° - 21°" in page
    assert "4.1 meter/sec" in page
    assert "clouds.png" in page


def test_forecast_keeps_next_four_noon_entries(client):
    with patch("requests.get", side_effect=fake_owm()):
        page = client.get("/london").get_data(as_text=True)
    assert page.count('class="forecast-item"') == 5  # today + 4 forecast days
    for temp in (21, 22, 23, 24):
        assert f"{temp}º" in page
    assert "99º" not in page  # today's noon entry is excluded
    assert "55º" not in page  # non-noon entries are excluded
    assert "25º" not in page  # capped at four days
    tomorrow = today() + datetime.timedelta(days=1)
    assert tomorrow.strftime("%a") in page


def test_unknown_city_shows_error_page(client):
    with patch("requests.get", side_effect=fake_owm(geo=[])):
        response = client.get("/nowheresville", follow_redirects=True)
    assert "This city does not exist" in response.get_data(as_text=True)
