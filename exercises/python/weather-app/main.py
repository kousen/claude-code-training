"""Flask weather app: search for a city, see current conditions and a short forecast."""
import datetime
import os
import string

import requests
from dotenv import load_dotenv
from flask import Flask, redirect, render_template, request, url_for

import weather

load_dotenv()
if not os.getenv("OWM_API_KEY"):
    raise RuntimeError("OWM_API_KEY is not set. Add it to a .env file or export it before starting the app.")

app = Flask(__name__)


@app.route("/", methods=["GET", "POST"])
def home():
    """Show the search form; on submit, redirect to the page for the city entered."""
    if request.method == "POST":
        city = request.form.get("search", "").strip()
        if city:
            return redirect(url_for("get_weather", city=city))
    return render_template("index.html")


@app.route("/<city>")
def get_weather(city: str):
    """Show current weather and a four-day forecast for a city, or a 404 page if it's unknown."""
    city_name = string.capwords(city)
    coordinates = weather.geocode(city_name)
    if coordinates is None:
        return render_template("error.html"), 404

    current = weather.current_weather(*coordinates)
    # The city's local time: UTC plus the offset OpenWeather reports for the location
    today = datetime.datetime.now(datetime.timezone.utc) + current["utc_offset"]
    forecast = weather.forecast(*coordinates, current["utc_offset"], today.date())

    return render_template("city.html", city_name=city_name, current=current, forecast=forecast,
                           current_date=today.strftime("%A, %B %d"), today_label=today.strftime("%a"))


@app.errorhandler(requests.RequestException)
def weather_service_error(exc: requests.RequestException):
    """Turn any failed OpenWeather call (network error, timeout, bad key, 5xx) into a 502 page."""
    # Log only the exception type: the message contains the request URL, which includes the API key
    app.logger.error("OpenWeather request failed: %s", type(exc).__name__)
    message = "The weather service is unavailable. Please try again later."
    return render_template("error.html", message=message), 502


if __name__ == "__main__":
    app.run()
