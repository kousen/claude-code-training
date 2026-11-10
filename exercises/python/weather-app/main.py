import datetime
import os
import string

import requests
from dotenv import load_dotenv
from flask import Flask, redirect, render_template, request, url_for

load_dotenv()

OWM_ENDPOINT = "https://api.openweathermap.org/data/2.5/weather"
OWM_FORECAST_ENDPOINT = "https://api.openweathermap.org/data/2.5/forecast"
GEOCODING_API_ENDPOINT = "https://api.openweathermap.org/geo/1.0/direct"
TIMEOUT = 10  # seconds, for every OpenWeather call

# Weather conditions that have a matching static/assets/<name>.png
ICONS = {"clear", "clouds", "drizzle", "fog", "haze", "mist", "rain", "smoke", "snow", "thunderstorm", "tornado"}
DEFAULT_ICON = "clouds"

api_key = os.getenv("OWM_API_KEY")
if not api_key:
    raise RuntimeError("OWM_API_KEY is not set. Add it to a .env file or export it before starting the app.")

app = Flask(__name__)


def icon_for(condition):
    """Return the icon name for an OpenWeather condition, falling back for ones without artwork."""
    name = condition.lower()
    return name if name in ICONS else DEFAULT_ICON


# Display home page and get city name entered into search form
@app.route("/", methods=["GET", "POST"])
def home():
    if request.method == "POST":
        city = request.form.get("search", "").strip()
        if city:
            return redirect(url_for("get_weather", city=city))
    return render_template("index.html")


# Display weather forecast for specific city using data from OpenWeather API
@app.route("/<city>", methods=["GET", "POST"])
def get_weather(city):
    city_name = string.capwords(city)

    # Get latitude and longitude for city
    location_params = {
        "q": city_name,
        "appid": api_key,
        "limit": 1,
    }
    location_response = requests.get(GEOCODING_API_ENDPOINT, params=location_params, timeout=TIMEOUT)
    location_response.raise_for_status()
    location_data = location_response.json()

    # No coordinates means the city name was not recognised
    if not location_data:
        return render_template("error.html"), 404
    lat = location_data[0]['lat']
    lon = location_data[0]['lon']

    # Get OpenWeather API data
    weather_params = {
        "lat": lat,
        "lon": lon,
        "appid": api_key,
        "units": "metric",
    }
    weather_response = requests.get(OWM_ENDPOINT, weather_params, timeout=TIMEOUT)
    weather_response.raise_for_status()
    weather_data = weather_response.json()

    # The city's local time: UTC plus the offset OpenWeather reports for the location
    utc = datetime.timezone.utc
    offset = datetime.timedelta(seconds=weather_data["timezone"])
    today = datetime.datetime.now(utc) + offset
    current_date = today.strftime("%A, %B %d")

    # Get current weather data
    current_temp = round(weather_data['main']['temp'])
    current_weather = weather_data['weather'][0]['main']
    min_temp = round(weather_data['main']['temp_min'])
    max_temp = round(weather_data['main']['temp_max'])
    wind_speed = weather_data['wind']['speed']

    # Get five-day weather forecast data
    forecast_response = requests.get(OWM_FORECAST_ENDPOINT, weather_params, timeout=TIMEOUT)
    forecast_response.raise_for_status()
    forecast_data = forecast_response.json()

    # Midday forecast for the next four days, in the city's local time. Entries are three
    # hours apart, so exactly one per day falls in the 11:00-13:59 window.
    forecast = []
    for item in forecast_data["list"]:
        local = datetime.datetime.fromtimestamp(item["dt"], utc) + offset
        if local.date() > today.date() and 11 <= local.hour < 14:
            forecast.append({
                "day": local.strftime("%a"),
                "temp": round(item["main"]["temp"]),
                "icon": icon_for(item["weather"][0]["main"]),
            })
    forecast = forecast[:4]

    return render_template("city.html", city_name=city_name, current_date=current_date, current_temp=current_temp,
                           current_weather=current_weather, current_icon=icon_for(current_weather),
                           min_temp=min_temp, max_temp=max_temp, wind_speed=wind_speed,
                           today_label=today.strftime("%a"), forecast=forecast)


# Any failed OpenWeather call (network error, timeout, bad key, 5xx) ends up here
@app.errorhandler(requests.RequestException)
def weather_service_error(exc):
    # Log only the exception type: the message contains the request URL, which includes the API key
    app.logger.error("OpenWeather request failed: %s", type(exc).__name__)
    message = "The weather service is unavailable. Please try again later."
    return render_template("error.html", message=message), 502


if __name__ == "__main__":
    app.run()
