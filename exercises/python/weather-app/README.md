# Weather App

A Flask web application that displays current weather and a 5-day forecast for any city, powered by the OpenWeatherMap API. This project is used as a training exercise for Claude Code.

## Prerequisites

- Python 3.x
- An [OpenWeatherMap](https://openweathermap.org/) API key (free tier works)

## Setup

1. Create and activate a virtual environment:

   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

2. Install dependencies:

   ```bash
   pip install -r requirements.txt
   ```

3. Create a `.env` file in the project root with your API key:

   ```
   OWM_API_KEY=your-api-key-here
   ```

## Running the App

```bash
python main.py
```

The app starts in debug mode at `http://127.0.0.1:5000`.

Alternatively, use Flask's CLI:

```bash
flask --app main run
```

## Project Structure

```
weather-app/
  main.py              # Flask application and route handlers
  requirements.txt     # Python dependencies
  .env                 # API key (not committed)
  templates/
    index.html         # Home page with search form
    city.html          # Weather display page
    error.html         # Error page
  static/
    css/main.css       # Stylesheet
    assets/            # Weather icons and background images
```

## Routes

| Method     | Path       | Description                              |
|------------|------------|------------------------------------------|
| GET        | `/`        | Home page with city search form          |
| POST       | `/`        | Submits search, redirects to `/<city>`   |
| GET        | `/<city>`  | Displays current weather and forecast    |
| GET        | `/error`   | Generic error page                       |

## How It Works

1. The user searches for a city on the home page.
2. The app geocodes the city name to coordinates using the OpenWeatherMap Geocoding API.
3. Current weather is fetched from the OWM Weather endpoint.
4. A 5-day forecast is fetched from the OWM Forecast endpoint (degrades gracefully if unavailable).
5. Results are rendered in the `city.html` template.

## Error Handling

The app uses a custom exception hierarchy:

- **WeatherAppError** -- base exception for all app-specific errors
  - **CityNotFoundError** -- raised when geocoding returns no results
  - **WeatherAPIError** -- raised on API timeouts, connection failures, or unexpected responses
  - **APIKeyMissingError** -- raised when `OWM_API_KEY` is not set

Flask error handlers catch these exceptions and render user-friendly messages via `error.html`. Standard 404 and 500 errors are also handled.

## Training Exercise

See [EXERCISE.md](EXERCISE.md) for guided tasks including code exploration, test generation, error handling improvements, and documentation exercises.
