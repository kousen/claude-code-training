"""OpenWeather API client: plain functions that return just what the pages need.

Every function that calls OpenWeather raises requests.RequestException on a network failure, timeout or
non-2xx response. The Flask app turns that into an error page in one place.
"""
from __future__ import annotations

import datetime
import os
from typing import Any

import requests

GEOCODING_URL = "https://api.openweathermap.org/geo/1.0/direct"
CURRENT_URL = "https://api.openweathermap.org/data/2.5/weather"
FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast"
TIMEOUT = 10  # seconds, for every call

# Weather conditions that have a matching static/assets/<name>.png
ICONS = {"clear", "clouds", "drizzle", "fog", "haze", "mist", "rain", "smoke", "snow", "thunderstorm", "tornado"}
DEFAULT_ICON = "clouds"

UTC = datetime.timezone.utc


def _get(url: str, **params: Any) -> Any:
    """GET an OpenWeather endpoint with the API key added, and return the decoded JSON."""
    params["appid"] = os.environ["OWM_API_KEY"]
    response = requests.get(url, params=params, timeout=TIMEOUT)
    response.raise_for_status()
    return response.json()


def icon_for(condition: str) -> str:
    """Return the icon name for a condition, falling back for ones without artwork."""
    name = condition.lower()
    return name if name in ICONS else DEFAULT_ICON


def geocode(city: str) -> tuple[float, float] | None:
    """Return (latitude, longitude) for a city name, or None if OpenWeather doesn't know it."""
    matches = _get(GEOCODING_URL, q=city, limit=1)
    if not matches:
        return None
    return matches[0]["lat"], matches[0]["lon"]


def current_weather(lat: float, lon: float) -> dict[str, Any]:
    """Return current conditions in metric units, plus the location's offset from UTC."""
    data = _get(CURRENT_URL, lat=lat, lon=lon, units="metric")
    condition = data["weather"][0]["main"]
    return {
        "temp": round(data["main"]["temp"]),
        "min_temp": round(data["main"]["temp_min"]),
        "max_temp": round(data["main"]["temp_max"]),
        "wind_speed": data["wind"]["speed"],
        "condition": condition,
        "icon": icon_for(condition),
        "utc_offset": datetime.timedelta(seconds=data["timezone"]),
    }


def forecast(lat: float, lon: float, utc_offset: datetime.timedelta, today: datetime.date) -> list[dict[str, Any]]:
    """Return the midday forecast for the four days after `today`, in the location's local time.

    OpenWeather returns entries three hours apart, so exactly one per day falls in
    the 11:00-13:59 local window.
    """
    data = _get(FORECAST_URL, lat=lat, lon=lon, units="metric")
    days = []
    for item in data["list"]:
        local = datetime.datetime.fromtimestamp(item["dt"], UTC) + utc_offset
        if local.date() > today and 11 <= local.hour < 14:
            condition = item["weather"][0]["main"]
            days.append({
                "day": local.strftime("%a"),
                "temp": round(item["main"]["temp"]),
                "condition": condition,
                "icon": icon_for(condition),
            })
    return days[:4]
