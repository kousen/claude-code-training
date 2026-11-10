# Weather App Architecture

## Components

The browser, the two Flask routes and the error handler, the OpenWeather client module, the templates and static assets, and the three OpenWeather endpoints.

```mermaid
flowchart TD
    Browser["Browser"]
    Env[".env file<br/>OWM_API_KEY"]

    subgraph Flask["Flask app: main.py"]
        Home["home()<br/>GET, POST /"]
        Weather["get_weather(city)<br/>GET /#lt;city#gt;"]
        Handler["weather_service_error()<br/>handles RequestException"]
    end

    subgraph Client["OpenWeather client: weather.py"]
        Geocode["geocode(city)"]
        CurrentFn["current_weather(lat, lon)"]
        ForecastFn["forecast(lat, lon, utc_offset, today)"]
    end

    subgraph Templates["templates/ (Jinja2)"]
        BaseT["base.html<br/>head, container, footer"]
        IndexT["index.html<br/>search form"]
        CityT["city.html<br/>current weather + forecast"]
        ErrorT["error.html<br/>not found / unavailable"]
    end

    subgraph Static["static/"]
        CSS["css/main.css<br/>grid, flexbox, media queries"]
        Icons["assets/*.png<br/>one icon per weather condition"]
    end

    subgraph OWM["OpenWeather API (HTTPS)"]
        Geo["Geocoding<br/>/geo/1.0/direct"]
        Current["Current weather<br/>/data/2.5/weather"]
        Forecast["Forecast, 3-hourly<br/>/data/2.5/forecast"]
    end

    Browser -->|"HTTP"| Home
    Browser -->|"HTTP"| Weather
    Browser -->|"loads"| Static

    Home -->|"renders"| IndexT
    Home -->|"POST: redirect"| Weather
    Weather -->|"renders"| CityT
    Weather -->|"unknown city: 404"| ErrorT
    Handler -->|"502"| ErrorT

    Weather -->|"1"| Geocode
    Weather -->|"2"| CurrentFn
    Weather -->|"3"| ForecastFn
    Geocode --> Geo
    CurrentFn --> Current
    ForecastFn --> Forecast
    Client -.->|"raises RequestException"| Handler

    IndexT -.->|"extends"| BaseT
    CityT -.->|"extends"| BaseT
    ErrorT -.->|"extends"| BaseT

    Env -.->|"load_dotenv()"| Flask
    CityT -.->|"icon name, with a default"| Icons
```

## Request sequence

One city search from form submit to rendered page, including the unknown-city and service-failure branches.

```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant Flask as Flask app (main.py)
    participant Client as weather.py
    participant OWM as OpenWeather API

    User->>Browser: Type a city, press Enter
    Browser->>Flask: POST / (search=city)
    Flask-->>Browser: 302 redirect to /city
    Browser->>Flask: GET /city

    Flask->>Client: geocode(city)
    Client->>OWM: GET /geo/1.0/direct (q, limit=1)
    OWM-->>Client: JSON list of matches

    alt No matches
        Client-->>Flask: None
        Flask-->>Browser: 404 error.html (city does not exist)
    else Match found
        Client-->>Flask: lat, lon
        Flask->>Client: current_weather(lat, lon)
        Client->>OWM: GET /data/2.5/weather (units=metric)
        OWM-->>Client: temp, min, max, wind, condition, timezone
        Client-->>Flask: current conditions + UTC offset
        Note over Flask: City-local today = UTC now + offset
        Flask->>Client: forecast(lat, lon, utc_offset, today)
        Client->>OWM: GET /data/2.5/forecast (units=metric)
        OWM-->>Client: 3-hourly entries for 5 days
        Note over Client: Keep the midday entry for each of the next 4 local days
        Client-->>Flask: four forecast days
        Flask-->>Browser: 200 city.html
    end

    opt Any OpenWeather call fails (network, timeout, bad key, 5xx)
        Client-->>Flask: raises RequestException
        Flask-->>Browser: 502 error.html (service unavailable)
    end

    Browser-->>User: Page
```

## `get_weather()` flow

The decision logic for the `/<city>` route across `main.py` and `weather.py`.

```mermaid
flowchart TD
    Start(["GET /#lt;city#gt;"]) --> Format["Capitalize city name"]
    Format --> GeoCall["geocode(city)<br/>Geocoding API, limit = 1"]
    GeoCall --> Found{"Any match?"}
    Found -->|"No"| NotFound(["Render error.html, 404"])
    Found -->|"Yes"| CurCall["current_weather(lat, lon)<br/>temp, min, max, wind, condition, UTC offset"]
    CurCall --> Today["City-local today =<br/>UTC now + offset"]
    Today --> FcCall["forecast(lat, lon, utc_offset, today)"]
    FcCall --> Filter["Convert each entry to local time<br/>Keep 11:00-13:59 on days after today"]
    Filter --> Slice["Take the first 4<br/>Pick icon, default if no artwork"]
    Slice --> Render(["Render city.html<br/>today + 4 forecast days"])

    GeoCall -.->|"request fails"| Fail
    CurCall -.->|"request fails"| Fail
    FcCall -.->|"request fails"| Fail
    Fail(["weather_service_error()<br/>log exception type, render error.html, 502"])
```
