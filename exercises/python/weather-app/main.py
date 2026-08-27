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
GEOCODING_API_ENDPOINT = "http://api.openweathermap.org/geo/1.0/direct"
api_key = os.getenv("OWM_API_KEY")
# api_key = os.environ.get("OWM_API_KEY")

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)
API_TIMEOUT = 10  # seconds per OpenWeather call

# OpenWeather's geocoder needs "city,state,country" for US states; a bare "city,state"
# is read as "city,country" and returns nothing. Map codes and full names -> code.
US_STATES = {
    "AL": "Alabama", "AK": "Alaska", "AZ": "Arizona", "AR": "Arkansas", "CA": "California",
    "CO": "Colorado", "CT": "Connecticut", "DE": "Delaware", "FL": "Florida", "GA": "Georgia",
    "HI": "Hawaii", "ID": "Idaho", "IL": "Illinois", "IN": "Indiana", "IA": "Iowa",
    "KS": "Kansas", "KY": "Kentucky", "LA": "Louisiana", "ME": "Maine", "MD": "Maryland",
    "MA": "Massachusetts", "MI": "Michigan", "MN": "Minnesota", "MS": "Mississippi", "MO": "Missouri",
    "MT": "Montana", "NE": "Nebraska", "NV": "Nevada", "NH": "New Hampshire", "NJ": "New Jersey",
    "NM": "New Mexico", "NY": "New York", "NC": "North Carolina", "ND": "North Dakota", "OH": "Ohio",
    "OK": "Oklahoma", "OR": "Oregon", "PA": "Pennsylvania", "RI": "Rhode Island", "SC": "South Carolina",
    "SD": "South Dakota", "TN": "Tennessee", "TX": "Texas", "UT": "Utah", "VT": "Vermont",
    "VA": "Virginia", "WA": "Washington", "WV": "West Virginia", "WI": "Wisconsin", "WY": "Wyoming",
    "DC": "District of Columbia",
}
_STATE_LOOKUP = {code: code for code in US_STATES} | {name.upper(): code for code, name in US_STATES.items()}


def normalize_query(query):
    """Turn 'Springfield, IL' or 'Springfield, Illinois' into 'Springfield,IL,US'.
    Anything else (bare city, city,country, city,state,country) is returned trimmed."""
    parts = [p.strip() for p in query.split(",") if p.strip()]
    if len(parts) == 2:
        code = _STATE_LOOKUP.get(parts[1].upper())
        if code:
            return f"{parts[0]},{code},US"
    return ",".join(parts)


# Display home page and get city name entered into search form
@app.route("/", methods=["GET", "POST"])
def home():
    if request.method == "POST":
        city = request.form.get("search")
        return redirect(url_for("get_weather", city=city))
    return render_template("index.html")


# Display weather forecast for specific city using data from OpenWeather API
@app.route("/weather/<city>")
def get_weather(city):
    # Format city name and get current date to display on page
    city_name = string.capwords(city)
    today = datetime.datetime.now()
    current_date = today.strftime("%A, %B %d")

    try:
        # Get latitude and longitude for city
        location_params = {
            "q": normalize_query(city),
            "appid": api_key,
            "limit": 3,
        }
        location_response = requests.get(GEOCODING_API_ENDPOINT, params=location_params, timeout=API_TIMEOUT)
        location_response.raise_for_status()
        location_data = location_response.json()

        # Empty list means the geocoder found no coordinates for that name
        if not location_data:
            return redirect(url_for("error"))
        place = location_data[0]
        lat, lon = place['lat'], place['lon']

        # US locations get Fahrenheit/mph straight from the API; everyone else Celsius/m/s
        is_us = place.get('country') == 'US'
        units = "imperial" if is_us else "metric"
        if is_us and place.get('state'):
            city_name = f"{place['name']}, {place['state']}"

        # Get OpenWeather API data
        weather_params = {
            "lat": lat,
            "lon": lon,
            "appid": api_key,
            "units": units,
        }
        weather_response = requests.get(OWM_ENDPOINT, weather_params, timeout=API_TIMEOUT)
        weather_response.raise_for_status()
        weather_data = weather_response.json()

        # Get five-day weather forecast data
        forecast_response = requests.get(OWM_FORECAST_ENDPOINT, weather_params, timeout=API_TIMEOUT)
        forecast_response.raise_for_status()
        forecast_data = forecast_response.json()
    except requests.RequestException as e:
        # Covers connection errors, timeouts, and non-2xx responses (e.g. 401 for a bad API key)
        app.logger.error("OpenWeather request failed for %r: %s", city_name, e)
        return redirect(url_for("error", reason="api"))

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

    return render_template("city.html", city_name=city_name, current_date=current_date, current_temp=current_temp,
                           current_weather=current_weather, min_temp=min_temp, max_temp=max_temp, wind_speed=wind_speed,
                           today_label=today.strftime("%a"), forecast=forecast,
                           temp_unit="F" if is_us else "C", wind_unit="mph" if is_us else "meter/sec")


# Display error page for invalid input
@app.route("/error")
def error():
    if request.args.get("reason") == "api":
        message = "Weather service unavailable. Please try again later."
    else:
        message = "This city does not exist. Please try again."
    return render_template("error.html", message=message)


if __name__ == "__main__":
    app.run(debug=True)
