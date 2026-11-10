# Weather App Architecture

## Components

The browser, the three Flask routes, the templates and static assets they use, and the three OpenWeather endpoints.

```mermaid
flowchart TD
    Browser["Browser"]
    Env[".env file<br/>OWM_API_KEY"]

    subgraph Flask["Flask app: main.py"]
        Home["home()<br/>GET, POST /"]
        Weather["get_weather(city)<br/>GET /#lt;city#gt;"]
        Error["error()<br/>GET /error"]
    end

    subgraph Templates["templates/ (Jinja2)"]
        IndexT["index.html<br/>search form"]
        CityT["city.html<br/>current weather + forecast"]
        ErrorT["error.html<br/>city not found"]
    end

    subgraph Static["static/"]
        CSS["css/main.css<br/>grid, flexbox, media queries"]
        Icons["assets/*.png<br/>one icon per weather condition"]
    end

    subgraph OWM["OpenWeather API"]
        Geo["Geocoding<br/>/geo/1.0/direct"]
        Current["Current weather<br/>/data/2.5/weather"]
        Forecast["Forecast, 3-hourly<br/>/data/2.5/forecast"]
    end

    Browser -->|"HTTP"| Home
    Browser -->|"HTTP"| Weather
    Browser -->|"HTTP"| Error
    Browser -->|"loads"| Static

    Home -->|"renders"| IndexT
    Home -->|"POST: redirect"| Weather
    Weather -->|"renders"| CityT
    Weather -->|"no match: redirect"| Error
    Error -->|"renders"| ErrorT

    Weather -->|"1. city name"| Geo
    Weather -->|"2. lat, lon"| Current
    Weather -->|"3. lat, lon"| Forecast

    Env -.->|"load_dotenv()"| Flask
    CityT -.->|"icon chosen by condition name"| Icons
```

## Request sequence

One city search from form submit to rendered page, including the no-match branch.

```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant Flask as Flask app (main.py)
    participant Geo as OpenWeather Geocoding
    participant Current as OpenWeather Current
    participant Forecast as OpenWeather Forecast

    User->>Browser: Type a city, press Enter
    Browser->>Flask: POST / (search=city)
    Flask-->>Browser: 302 redirect to /city
    Browser->>Flask: GET /city

    Flask->>Geo: GET /geo/1.0/direct (q, limit=3, appid)
    Geo-->>Flask: JSON list of matches

    alt No matches
        Flask-->>Browser: 302 redirect to /error
        Browser->>Flask: GET /error
        Flask-->>Browser: error.html
    else At least one match
        Note over Flask: Use lat and lon of the first match
        Flask->>Current: GET /data/2.5/weather (lat, lon, units=metric)
        Current-->>Flask: temp, min, max, wind, condition
        Flask->>Forecast: GET /data/2.5/forecast (lat, lon, units=metric)
        Forecast-->>Flask: 3-hourly entries for 5 days
        Note over Flask: Keep the 12:00 entries for the next 4 days
        Flask-->>Browser: city.html
        Browser->>Flask: GET /static css and icons
        Flask-->>Browser: Static files
    end

    Browser-->>User: Weather page
```

## `get_weather()` flow

The decision logic inside the `/<city>` route in `main.py`.

```mermaid
flowchart TD
    Start(["GET /#lt;city#gt;"]) --> Format["Capitalize city name<br/>Format today's date"]
    Format --> GeoCall["Call Geocoding API<br/>q = city, limit = 3"]
    GeoCall --> Found{"Any matches?"}
    Found -->|"No"| ErrorPage(["Redirect to /error"])
    Found -->|"Yes"| Coords["Take lat and lon<br/>of the first match"]
    Coords --> CurCall["Call Current Weather API<br/>units = metric"]
    CurCall --> Status{"HTTP status OK?"}
    Status -->|"No"| Raise(["raise_for_status()<br/>HTTPError, 500 response"])
    Status -->|"Yes"| Extract["Extract temp, min, max,<br/>wind speed, condition"]
    Extract --> FcCall["Call Forecast API<br/>same lat, lon, units"]
    FcCall --> Filter["Keep entries at 12:00:00<br/>that are not today's date"]
    Filter --> Slice["Take the first 4<br/>Label each by its own date"]
    Slice --> Render(["Render city.html<br/>today + 4 forecast days"])
```
