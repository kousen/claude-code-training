import datetime
import requests
import string
from flask import Flask, render_template, request, redirect, url_for
import os
from dotenv import load_dotenv
load_dotenv()

OWM_ENDPOINT = "https://api.openweathermap.org/data/2.5/weather"
OWM_FORECAST_ENDPOINT = "https://api.openweathermap.org/data/2.5/forecast"
GEOCODING_API_ENDPOINT = "http://api.openweathermap.org/geo/1.0/direct"
api_key = os.getenv("OWM_API_KEY")
# api_key = os.environ.get("OWM_API_KEY")

app = Flask(__name__)

if not api_key:
    app.logger.warning("OWM_API_KEY is not set; every OpenWeather call will be rejected with 401")

REQUEST_TIMEOUT = (3, 5)  # (connect, read) seconds; without this a stalled call hangs the worker
SESSION = requests.Session()  # reuse the TLS connection across the three calls per page


def fetch_json(url, params):
    """GET ``url`` and return the decoded JSON, raising on HTTP errors and timeouts."""
    response = SESSION.get(url, params=params, timeout=REQUEST_TIMEOUT)
    response.raise_for_status()
    return response.json()


# Display home page and get city name entered into search form
@app.route("/", methods=["GET", "POST"])
def home():
    if request.method == "POST":
        city = request.form.get("search")
        return redirect(url_for("get_weather", city=city))
    return render_template("index.html")


# Display weather forecast for specific city using data from OpenWeather API
@app.route("/<city>", methods=["GET", "POST"])
def get_weather(city):
    # Format city name and get current date to display on page
    city_name = string.capwords(city)
    today = datetime.datetime.now()
    current_date = today.strftime("%A, %B %d")

    # Get latitude and longitude for city
    location_params = {
        "q": city_name,
        "appid": api_key,
        "limit": 3,
    }

    try:
        location_data = fetch_json(GEOCODING_API_ENDPOINT, location_params)
        if not location_data:
            app.logger.info("No coordinates found for %r", city_name)
            return redirect(url_for("error"))
        lat = location_data[0]['lat']
        lon = location_data[0]['lon']

        weather_params = {
            "lat": lat,
            "lon": lon,
            "appid": api_key,
            "units": "metric",
        }
        weather_data = fetch_json(OWM_ENDPOINT, weather_params)
        forecast_data = fetch_json(OWM_FORECAST_ENDPOINT, weather_params)

        # Get current weather data
        current_temp = round(weather_data['main']['temp'])
        current_weather = weather_data['weather'][0]['main']
        min_temp = round(weather_data['main']['temp_min'])
        max_temp = round(weather_data['main']['temp_max'])
        wind_speed = weather_data['wind']['speed']

        # Noon forecast for the next four days. Derive the day label from each entry's own
        # date so labels and data can't drift apart (today's noon entry is absent after 12:00 UTC).
        today_str = today.strftime("%Y-%m-%d")
        forecast = [
            {
                "day": datetime.datetime.strptime(item["dt_txt"], "%Y-%m-%d %H:%M:%S").strftime("%a"),
                "temp": round(item["main"]["temp"]),
                "weather": item["weather"][0]["main"],
            }
            for item in forecast_data["list"]
            if item["dt_txt"].endswith("12:00:00") and not item["dt_txt"].startswith(today_str)
        ][:4]
    except requests.HTTPError as exc:
        if exc.response is not None and exc.response.status_code == 401:
            app.logger.error("OpenWeather rejected the API key; check OWM_API_KEY")
            return redirect(url_for("error", reason="auth"))
        app.logger.exception("OpenWeather request failed for %r", city_name)
        return redirect(url_for("error", reason="unavailable"))
    except requests.RequestException:
        # Timeouts and connection failures
        app.logger.exception("OpenWeather request failed for %r", city_name)
        return redirect(url_for("error", reason="unavailable"))
    except (KeyError, IndexError, TypeError):
        # 200 OK but the body isn't shaped the way we expect
        app.logger.exception("Unexpected OpenWeather payload for %r", city_name)
        return redirect(url_for("error", reason="unavailable"))

    return render_template("city.html", city_name=city_name, current_date=current_date, current_temp=current_temp,
                           current_weather=current_weather, min_temp=min_temp, max_temp=max_temp, wind_speed=wind_speed,
                           today_label=today.strftime("%a"), forecast=forecast)


# Display error page for invalid input
@app.route("/error")
def error():
    messages = {
        "unavailable": "The weather service is unavailable right now. Please try again later.",
        "auth": "The weather service rejected this app's API key. Check the OWM_API_KEY setting.",
    }
    message = messages.get(request.args.get("reason"), "This city does not exist. Please try again.")
    return render_template("error.html", message=message)


if __name__ == "__main__":
    app.run(debug=True)
