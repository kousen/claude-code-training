"""Tests for the weather app. All OpenWeather calls are mocked; no network needed."""
import datetime
import os
from unittest.mock import Mock, patch

import pytest
import requests

os.environ.setdefault("OWM_API_KEY", "test-key")

import main  # noqa: E402

UTC = datetime.timezone.utc
AUCKLAND = 13 * 3600  # seconds east of UTC


def local_today(offset=0):
    """Today's date in a place `offset` seconds east of UTC."""
    return (datetime.datetime.now(UTC) + datetime.timedelta(seconds=offset)).date()


def forecast_entries(offset=0):
    """09:00 and 12:00 local-time entries for today and the next five days.

    Midday temps: today 99, then 21..25. All 09:00 entries are 55.
    """
    entries = []
    for day_offset, noon_temp in enumerate([99, 21, 22, 23, 24, 25]):
        day = local_today(offset) + datetime.timedelta(days=day_offset)
        for hour, temp in ((9, 55), (12, noon_temp)):
            local = datetime.datetime.combine(day, datetime.time(hour), UTC)
            entries.append({
                "dt": int(local.timestamp()) - offset,
                "main": {"temp": temp},
                "weather": [{"main": "Rain"}],
            })
    return entries


def fake_owm(geo=None, offset=0, condition="Clouds", status_error=None):
    """Build a stand-in for requests.get that answers by URL."""
    payloads = {
        "geo/1.0/direct": [{"lat": 51.5, "lon": -0.12}] if geo is None else geo,
        "data/2.5/weather": {
            "main": {"temp": 18.4, "temp_min": 15.2, "temp_max": 20.6},
            "weather": [{"main": condition}],
            "wind": {"speed": 4.1},
            "timezone": offset,
        },
        "data/2.5/forecast": {"list": forecast_entries(offset)},
    }

    def get(url, *args, **kwargs):
        assert url.startswith("https://"), "API key must not travel over plain HTTP"
        assert kwargs.get("timeout"), "every OpenWeather call needs a timeout"
        response = Mock()
        response.json.return_value = next(v for k, v in payloads.items() if k in url)
        if status_error:
            response.raise_for_status.side_effect = status_error
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


def test_blank_search_stays_on_home_page(client):
    response = client.post("/", data={"search": "   "})
    assert response.status_code == 200
    assert b'name="search"' in response.data


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


@pytest.mark.parametrize("offset", [0, AUCKLAND, -8 * 3600, 5 * 3600 + 1800])
def test_forecast_keeps_next_four_midday_entries_in_city_time(client, offset):
    with patch("requests.get", side_effect=fake_owm(offset=offset)):
        page = client.get("/somewhere").get_data(as_text=True)
    assert page.count('class="forecast-item"') == 5  # today + 4 forecast days
    for temp in (21, 22, 23, 24):
        assert f"{temp}º" in page
    assert "99º" not in page  # today's midday entry is excluded
    assert "55º" not in page  # non-midday entries are excluded
    assert "25º" not in page  # capped at four days
    assert page.count('alt="Rain"') == 4  # forecast icons carry the condition as alt text
    today = local_today(offset)
    assert today.strftime("%A, %B %d") in page
    assert (today + datetime.timedelta(days=1)).strftime("%a") in page


def test_condition_without_artwork_uses_default_icon(client):
    with patch("requests.get", side_effect=fake_owm(condition="Squall")):
        page = client.get("/london").get_data(as_text=True)
    assert "Squall" in page  # the text still names the real condition
    assert "squall.png" not in page
    assert "clouds.png" in page


def test_unknown_city_shows_error_page_with_404(client):
    with patch("requests.get", side_effect=fake_owm(geo=[])):
        response = client.get("/nowheresville")
    assert response.status_code == 404
    assert "This city does not exist" in response.get_data(as_text=True)


def test_city_named_error_is_looked_up_like_any_other(client):
    with patch("requests.get", side_effect=fake_owm()):
        response = client.get("/error")
    assert response.status_code == 200
    assert "18ºC" in response.get_data(as_text=True)


@pytest.mark.parametrize("failure", [
    requests.ConnectionError("no network"),
    requests.Timeout("too slow"),
])
def test_network_failure_shows_service_unavailable(client, failure):
    with patch("requests.get", side_effect=failure):
        response = client.get("/london")
    assert response.status_code == 502
    assert "weather service is unavailable" in response.get_data(as_text=True)


def test_rejected_api_key_shows_service_unavailable_without_leaking_key(client, caplog):
    error = requests.HTTPError("401 Client Error for url: https://example.test/?appid=SECRET-KEY")
    with patch("requests.get", side_effect=fake_owm(status_error=error)):
        response = client.get("/london")
    assert response.status_code == 502
    assert "weather service is unavailable" in response.get_data(as_text=True)
    assert "SECRET-KEY" not in response.get_data(as_text=True)
    assert "SECRET-KEY" not in caplog.text
