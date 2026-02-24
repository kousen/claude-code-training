import datetime
import logging
import time
import string
import os

import requests
from flask import Flask, render_template, request, redirect, url_for
from dotenv import load_dotenv

load_dotenv()

# --- Logging configuration ---
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler()],
)
logger = logging.getLogger(__name__)


# --- Custom exceptions ---
class WeatherAppError(Exception):
    """Base exception for the weather app."""


class CityNotFoundError(WeatherAppError):
    """Raised when geocoding returns no results for a city."""


class WeatherAPIError(WeatherAppError):
    """Raised when the OpenWeatherMap API returns an error."""


class APIKeyMissingError(WeatherAppError):
    """Raised when the OWM_API_KEY environment variable is not set."""


# --- App configuration ---
OWM_ENDPOINT = "https://api.openweathermap.org/data/2.5/weather"
OWM_FORECAST_ENDPOINT = "https://api.openweathermap.org/data/2.5/forecast"
GEOCODING_API_ENDPOINT = "http://api.openweathermap.org/geo/1.0/direct"
API_TIMEOUT = 10  # seconds

api_key = os.getenv("OWM_API_KEY")
if not api_key:
    logger.warning("OWM_API_KEY is not set — API calls will fail until it is configured")

app = Flask(__name__)


# --- Flask error handlers ---
@app.errorhandler(404)
def page_not_found(_e):
    logger.info("404 — %s not found", request.path)
    return render_template("error.html", error_message="Page not found."), 404


@app.errorhandler(500)
def internal_error(e):
    logger.error("Unhandled 500 error: %s", e)
    return render_template("error.html", error_message="Something went wrong on our end. Please try again later."), 500


@app.errorhandler(WeatherAppError)
def handle_weather_error(e):
    logger.error("WeatherAppError: %s", e)
    return render_template("error.html", error_message=str(e))


# --- Routes ---
@app.route("/", methods=["GET", "POST"])
def home():
    if request.method == "POST":
        city = request.form.get("search")
        logger.info("Search submitted for city: %s", city)
        return redirect(url_for("get_weather", city=city))
    return render_template("index.html")


@app.route("/<city>", methods=["GET", "POST"])
def get_weather(city):
    logger.info("Weather request for city: %s", city)

    if not api_key:
        raise APIKeyMissingError("Weather service is not configured. Please set the OWM_API_KEY environment variable.")

    city_name = string.capwords(city)
    today = datetime.datetime.now()
    current_date = today.strftime("%A, %B %d")

    # --- Geocoding ---
    location_params = {"q": city_name, "appid": api_key, "limit": 3}

    try:
        start = time.monotonic()
        location_response = requests.get(GEOCODING_API_ENDPOINT, params=location_params, timeout=API_TIMEOUT)
        location_response.raise_for_status()
        location_data = location_response.json()
        logger.debug("Geocoding responded in %.2fs", time.monotonic() - start)
    except requests.exceptions.Timeout:
        raise WeatherAPIError("The geocoding service timed out. Please try again later.")
    except requests.exceptions.ConnectionError:
        raise WeatherAPIError("Could not connect to the geocoding service. Please check your internet connection.")
    except requests.exceptions.HTTPError as exc:
        logger.error("Geocoding HTTP error for '%s': %s", city_name, exc.response.status_code)
        raise WeatherAPIError("The geocoding service returned an error. Please try again later.")
    except (KeyError, ValueError) as exc:
        logger.error("Unexpected geocoding response for '%s': %s", city_name, exc)
        raise WeatherAPIError("Received an unexpected response from the geocoding service.")

    if not location_data:
        raise CityNotFoundError(f"City '{city_name}' was not found. Please check the spelling and try again.")

    try:
        lat = location_data[0]["lat"]
        lon = location_data[0]["lon"]
    except (KeyError, IndexError) as exc:
        logger.error("Malformed geocoding data for '%s': %s", city_name, exc)
        raise WeatherAPIError("Received unexpected data from the geocoding service.")

    # --- Current weather ---
    weather_params = {"lat": lat, "lon": lon, "appid": api_key, "units": "metric"}

    try:
        start = time.monotonic()
        weather_response = requests.get(OWM_ENDPOINT, params=weather_params, timeout=API_TIMEOUT)
        weather_response.raise_for_status()
        weather_data = weather_response.json()
        logger.debug("Current weather responded in %.2fs", time.monotonic() - start)
    except requests.exceptions.Timeout:
        raise WeatherAPIError("The weather service timed out. Please try again later.")
    except requests.exceptions.ConnectionError:
        raise WeatherAPIError("Could not connect to the weather service. Please check your internet connection.")
    except requests.exceptions.HTTPError as exc:
        logger.error("Weather HTTP error for '%s': %s", city_name, exc.response.status_code)
        raise WeatherAPIError("The weather service returned an error. Please try again later.")

    try:
        current_temp = round(weather_data["main"]["temp"])
        current_weather = weather_data["weather"][0]["main"]
        min_temp = round(weather_data["main"]["temp_min"])
        max_temp = round(weather_data["main"]["temp_max"])
        wind_speed = weather_data["wind"]["speed"]
    except (KeyError, IndexError) as exc:
        logger.error("Malformed weather data for '%s': %s", city_name, exc)
        raise WeatherAPIError("Received unexpected data from the weather service.")

    # --- 5-day forecast (graceful degradation) ---
    forecast_available = True
    five_day_temp_list = []
    five_day_weather_list = []
    five_day_dates_list = []

    try:
        start = time.monotonic()
        forecast_response = requests.get(OWM_FORECAST_ENDPOINT, params=weather_params, timeout=API_TIMEOUT)
        forecast_response.raise_for_status()
        forecast_data = forecast_response.json()
        logger.debug("Forecast responded in %.2fs", time.monotonic() - start)

        five_day_temp_list = [round(item["main"]["temp"]) for item in forecast_data["list"] if "12:00:00" in item["dt_txt"]]
        five_day_weather_list = [item["weather"][0]["main"] for item in forecast_data["list"] if "12:00:00" in item["dt_txt"]]

        five_day_unformatted = [today + datetime.timedelta(days=i) for i in range(5)]
        five_day_dates_list = [date.strftime("%a") for date in five_day_unformatted]
    except (requests.exceptions.RequestException, KeyError, IndexError) as exc:
        logger.error("Forecast unavailable for '%s': %s", city_name, exc)
        forecast_available = False

    logger.info("Successfully rendered weather for '%s'", city_name)

    return render_template(
        "city.html",
        city_name=city_name,
        current_date=current_date,
        current_temp=current_temp,
        current_weather=current_weather,
        min_temp=min_temp,
        max_temp=max_temp,
        wind_speed=wind_speed,
        five_day_temp_list=five_day_temp_list,
        five_day_weather_list=five_day_weather_list,
        five_day_dates_list=five_day_dates_list,
        forecast_available=forecast_available,
    )


@app.route("/error")
def error():
    return render_template("error.html")


if __name__ == "__main__":
    app.run(debug=True)
