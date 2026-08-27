# Weather App

A small Flask web app that shows current conditions and a five-day forecast for any city, using the [OpenWeather](https://openweathermap.org/api) geocoding, current-weather, and forecast APIs. Originally written by Rachana Hegde as a portfolio project (see [Origins](#origins)); it now serves as the Python exercise for the Claude Code training course, with tests, error handling, and US-city support added along the way.

## Quick start

Requires Python 3.11+ and a free OpenWeather API key.

```bash
python -m venv .venv
source .venv/bin/activate          # Windows: .venv\Scripts\activate
pip install -r requirements.txt
echo 'OWM_API_KEY=your-key-here' > .env
python main.py
```

Open <http://127.0.0.1:5000>, type a city, and submit. `main.py` runs Flask in debug mode with auto-reload; for production use the Procfile's `gunicorn main:app`.

## Usage

Search from the home page, or go straight to `/weather/<city>`. Accepted forms:

| Input | Result |
|---|---|
| `Paris` | Top geocoder hit (Paris, Ile-de-France) |
| `Springfield, IL` or `Springfield, Illinois` | Springfield, Illinois — the US-state form OpenWeather rejects on its own |
| `Springfield, IL, US` | Same, explicit |
| `Toronto, CA` / `Berlin, DE` | Canada / Germany — country codes that collide with state codes still work |
| `Nowhere, IL` | Error page: "This city does not exist" |

**Units** are chosen by the geocoder's country: US locations get °F and mph, everyone else °C and meter/sec. The heading shows the geocoder's own name and state/region, so `paris, tx` renders as "Paris, Texas".

**Forecast** tiles show today's current conditions followed by the 12:00 UTC forecast for the next four days.

## How it works

Everything lives in `main.py` (~150 lines), three routes:

| Route | Purpose |
|---|---|
| `GET/POST /` | Search form; POST redirects to `/weather/<city>` |
| `GET /weather/<city>` | Geocode → current weather → forecast → render `city.html` |
| `GET /error` | Error page; `?reason=api` selects the service-unavailable message |

Request flow for `/weather/<city>`:

1. **`geocode(query)`** calls `geo/1.0/direct` with the input as typed. If that returns nothing and the input looks like `City, ST` for a US state (`us_state_query`), it retries once as `City,ST,US`. Trying as-typed first matters because many state codes are also ISO country codes (`CA`, `DE`, `IN`, `GA`…).
2. The first result's `country` selects `units=imperial` or `metric`; its `name`/`state` become the page heading.
3. `data/2.5/weather` and `data/2.5/forecast` are called with the coordinates. Forecast entries are filtered to the `12:00:00` slot for each *future* day — today's noon slot disappears from the API after 12:00 UTC, so labels are derived from each entry's own date rather than counted from today.

All three HTTP calls have a 10-second timeout and `raise_for_status()`; any `requests.RequestException` (bad key, network down, timeout, 5xx) is logged with the query and redirected to `/error?reason=api`.

Stray root paths (`/favicon.ico`, `/robots.txt`) 404 at the router — they never reach the API. Pages declare a PNG favicon from `static/assets/`.

## Configuration

| Variable | Required | Notes |
|---|---|---|
| `OWM_API_KEY` | yes | OpenWeather API key. Read from the environment or `.env` via python-dotenv. A missing or invalid key surfaces as the service-unavailable error page with a `401` in the log. |

`.env` is gitignored. `API_TIMEOUT` (seconds) is a constant in `main.py`.

## Testing

```bash
python -m pytest
```

`pytest.ini` runs coverage on every invocation and fails the run below 80% (currently ~99%). All tests mock `requests.get`, so the suite runs offline in well under a second. Coverage includes the happy path, unit selection, the US-state retry and country-code collision, every failure branch, the after-noon forecast shape, and routing.

To exercise the real API, start the server and hit it with `curl`; the search term must be URL-encoded (`/weather/Springfield%2C%20IL`).

## Project layout

```
main.py             Flask app: routes, geocoding, OpenWeather calls
test_main.py        pytest suite (mocked HTTP)
pytest.ini          coverage config and 80% gate
templates/          index.html (search), city.html (weather), error.html
static/css/         main.css — responsive grid/flexbox layout
static/assets/      weather icons (named after OpenWeather's condition strings) and backgrounds
requirements.txt    Flask, requests, python-dotenv, gunicorn, pytest, pytest-cov (plus legacy Jupyter pins)
Procfile            gunicorn entry point
EXERCISE.md         Training-course tasks for this project
```

Weather icons are looked up as `static/assets/<condition>.png` using the lowercased `weather[0].main` value from the API (`Clear`, `Clouds`, `Rain`, …), so a new condition string needs a matching PNG.

## Known limitations

- Ambiguous names (`Springfield`, `Portland`) show whichever result OpenWeather ranks first; there is no chooser. Add the state to disambiguate.
- US territories (`PR`, `GU`, `VI`) geocode with their own country codes and get metric units.
- Min/max and forecast temperatures print a bare degree sign without the unit letter.
- `requirements.txt` still carries Jupyter-related pins from the original project that the app does not use.

## Origins

The sections below are the original author's write-up, kept for attribution and context.

## Screenshots (Desktop)
<img src="/screenshots/weather_app_desktop_home_page_screenshot.png">
<img src="/screenshots/weather_app_desktop_forecast_page_screenshot.png">
<img src="/screenshots/weather_app_desktop_error_page_screenshot.png">

## Screenshots (Mobile)
<img src="/screenshots/weather_app_iphone_forecast_page_screenshot.png" style="width:400px;"> <img src="/screenshots/weather_app_iphone_home_page_screenshot.png" style="width:400px;">

## Reflection
Building a Python project from scratch without relying on a tutorial taught me a lot but I was also able to implement the app's key functionality (getting and displaying weather data) due to the work I did with APIs in Angela Yu's Python bootcamp. While deploying this web app, I  learned about git and version control as well as storing API keys as environment variables with .env and the purpose of .gitignore. This project turned out to be frustrating and complicated at times but I learned and grew a lot as a developer by tackling each problem. For instance, I struggled to make this website responsive because I discovered that the Chrome browser tools are not entirely accurate for the mobile view. Hence, when the app was deployed, the website didn't look the way I expected ore desired on mobile. So I switched to a free desktop application called Responsively and it provided views for multiple devices which allowed me to improve my CSS. In addition, I had difficulty positioning my footer at the bottom of my page and had to refer to [this resource](https://stackoverflow.com/questions/51683107/making-a-footer-stay-at-the-bottom-of-the-page-both-in-mobile-view-and-desktop-v) to adjust my CSS accordingly. 

If I were to do this project again, there are a few changes I would make. I would make the website render beautifully on iPads as well by eliminating the space between elements. I also wasn't sure how to add more functionality to this app while maintaining a seamless UI/UX design this time and I would love to do so in the future. E.g. I could add an option for users to specify the country of the city they enter (if multiple countries share the same city name). I could also display the low and high temperatures for each day in the five day forecast instead of only displaying the temperature at noon (which I understand is not an accurate estimate of the overall temperature). It would also be useful to enable location detection to allow the user to get more accurate weather data for their current location by using their precise coordinates instead of relying on the geocoding API provided by OpenWeather.  

## Useful Resources
- [OpenWeather API Documentation](https://openweathermap.org/api/one-call-3)
- [Guide to Flexbox](https://css-tricks.com/snippets/css/a-guide-to-flexbox/)
- [Guide to CSS Grid](https://css-tricks.com/snippets/css/complete-guide-grid/)
- [StackOverflow Post on Centering in CSS Grid](https://stackoverflow.com/questions/45536537/centering-in-css-grid)
- [Youtube video on searching blog posts with Flask](https://www.youtube.com/watch?v=kmtZTo-_gJY)
- [Article on retrieving HTML form data with Flask](https://www.geeksforgeeks.org/retrieving-html-from-data-using-flask/)
- [StackOverflow post on building Flask app search bar](https://stackoverflow.com/questions/39960942/flask-app-search-bar)
- [Article on storing API keys as environment variables](https://jonathansoma.com/lede/foundations-2019/classes/apis/keeping-api-keys-secret/)
– [StackOverflow post on storing API keys in Heroku](https://stackoverflow.com/questions/71593743/storing-api-key-in-heroku)
- [Making a footer stay at the bottom of the page both in mobile view and desktop view](https://stackoverflow.com/questions/51683107/making-a-footer-stay-at-the-bottom-of-the-page-both-in-mobile-view-and-desktop-v)

## Image Credit
- [Home page background image](https://unsplash.com/photos/2KXEb_8G5vo)
- [Error page desktop background image](https://unsplash.com/photos/U-Kty6HxcQc)

### Icons 
- <a target="_blank" href="https://icons8.com/icon/pLiaaoa41R9n/wind">Wind</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
- <a target="_blank" href="https://icons8.com/icon/37802/thermometer">Thermometer</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
- <a target="_blank" href="https://icons8.com/icon/99328/clouds">Clouds</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
- <a target="_blank" href="https://icons8.com/icon/101829/rain">Rain</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
- <a target="_blank" href="https://icons8.com/icon/67657/fog">Fog</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
- <a target="_blank" href="https://icons8.com/icon/hXkspV0LTEoE/snow">Snow</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
- <a target="_blank" href="https://icons8.com/icon/G3xS4dQTvswX/rainy-weather">Rainy Weather</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
- <a target="_blank" href="https://icons8.com/icon/101843/storm">Storm</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
- <a target="_blank" href="https://icons8.com/icon/59878/search">Search</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
- <a target="_blank" href="https://icons8.com/icon/39789/chevron-left">Chevron Left</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
- <a target="_blank" href="https://icons8.com/icon/99362/summer">Summer</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
- <a target="_blank" href="https://icons8.com/icon/akbaie9da2Be/tornado">Tornado</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
