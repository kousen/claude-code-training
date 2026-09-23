import datetime
import logging
import requests
import string
from flask import Flask, render_template, request, redirect, url_for
import os
from dotenv import load_dotenv
load_dotenv()

OWM_ENDPOINT = "https://api.openweathermap.org/data/2.5/weather"
OWM_FORECAST_ENDPOINT = "https://api.openweathermap.org/data/2.5/forecast"
GEOCODING_API_ENDPOINT = "https://api.openweathermap.org/geo/1.0/direct"
REQUEST_TIMEOUT = 10  # seconds
SERVICE_ERROR = "Weather data is unavailable right now. Please try again later."
api_key = os.getenv("OWM_API_KEY")

app = Flask(__name__)
logger = logging.getLogger(__name__)


# Call an OpenWeather endpoint; raises requests.RequestException on network errors or non-2xx responses
def owm_get(url, params):
    response = requests.get(url, params=params, timeout=REQUEST_TIMEOUT)
    response.raise_for_status()
    return response.json()


# Noon forecast for the next four days. Derive the day label from each entry's own
# date so labels and data can't drift apart (today's noon entry is absent after 12:00 UTC).
def noon_forecast(entries, today_str):
    return [
        {
            "day": datetime.datetime.strptime(item["dt_txt"], "%Y-%m-%d %H:%M:%S").strftime("%a"),
            "temp": round(item["main"]["temp"]),
            "weather": item["weather"][0]["main"],
        }
        for item in entries
        if item["dt_txt"].endswith("12:00:00") and not item["dt_txt"].startswith(today_str)
    ][:4]


# Display home page and get city name entered into search form
@app.route("/", methods=["GET", "POST"])
def home():
    if request.method == "POST":
        city = request.form.get("search", "").strip()
        if not city:
            return redirect(url_for("home"))
        return redirect(url_for("get_weather", city=city))
    return render_template("index.html")


# Display weather forecast for specific city using data from OpenWeather API
@app.route("/<city>")
def get_weather(city):
    # Format city name and get current date to display on page
    city_name = string.capwords(city)
    today = datetime.datetime.now()
    current_date = today.strftime("%A, %B %d")

    try:
        # Get latitude and longitude for city
        location_data = owm_get(GEOCODING_API_ENDPOINT, {"q": city_name, "appid": api_key, "limit": 3})

        # No coordinates means the city doesn't exist
        if not location_data:
            return redirect(url_for("error"))

        weather_params = {
            "lat": location_data[0]["lat"],
            "lon": location_data[0]["lon"],
            "appid": api_key,
            "units": "metric",
        }
        weather_data = owm_get(OWM_ENDPOINT, weather_params)
        forecast_data = owm_get(OWM_FORECAST_ENDPOINT, weather_params)

        weather = {
            "current_temp": round(weather_data["main"]["temp"]),
            "current_weather": weather_data["weather"][0]["main"],
            "min_temp": round(weather_data["main"]["temp_min"]),
            "max_temp": round(weather_data["main"]["temp_max"]),
            "wind_speed": weather_data["wind"]["speed"],
            # dt_txt is UTC, so "today" must be the UTC date too
            "forecast": noon_forecast(forecast_data["list"],
                                      datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%d")),
        }
    except (requests.RequestException, KeyError, IndexError, TypeError) as e:
        # Don't log the exception message: HTTPError text includes the request URL, and with it the API key
        status = getattr(getattr(e, "response", None), "status_code", None)
        logger.error("OpenWeather request failed for city %r: %s (status %s)", city_name, type(e).__name__, status)
        return render_template("error.html", message=SERVICE_ERROR), 503

    return render_template("city.html", city_name=city_name, current_date=current_date,
                           today_label=today.strftime("%a"), **weather)


# Display error page for invalid input
@app.route("/error")
def error():
    return render_template("error.html")


# Browsers request this automatically; without it, /<city> would spend an API call geocoding "Favicon.ico"
@app.route("/favicon.ico")
def favicon():
    return "", 204


if __name__ == "__main__":
    app.run(debug=os.getenv("FLASK_DEBUG") == "1")
