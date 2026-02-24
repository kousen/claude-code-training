# Shopping Service Codebase - Reviewer Memory

## Project Identity
- Legacy Grails 2.4.3 / Groovy application, circa 2014 (SpringOne2GX demo)
- Used as a **training exercise** for modernization to Spring Boot 3.x
- Package: `s2gx`; App name: `shopping`
- 4 domain classes, 4 controllers, 8 Spock test files
- See `MODERNIZATION_PLAN.md` for the intended migration path

## Domain Model
- `Customer` — name (not blank), hasMany orders
- `Order` — number (unique), dateCreated/lastUpdated, belongsTo Customer, hasMany OrderLine
- `OrderLine` — product (ref), quantity (min 0), belongsTo Order (no cascade owner listed)
- `Product` — name (not blank), BigDecimal price (min 0.0)

## Known Issues (from first review, Feb 2026)
- `Order.getPrice()` uses `double` instead of `BigDecimal` — floating-point precision bug
- `OrderLine.getPrice()` uses `double` despite `Product.price` being `BigDecimal`
- `OrderLine.quantity` constraint is `min: 0` — allows zero-quantity lines
- `Order.getPrice()` throws NullPointerException on empty orderLines (`null.sum()` in Groovy)
- `OrderXmlRenderer` NPE if `order.customer` is null (no null guard)
- Production DB is H2 file-based (`jdbc:h2:prodDb`) — not suitable for real production
- `logSql = true` / `formatSql = true` left on in `DataSource.groovy` (performance + info leak)
- `:console:1.4.5` plugin included in compile scope — Groovy console exposed in production
- `CustomerControllerSpec` and `OrderLineControllerSpec` are empty stubs
- `OrderControllerSpec.populateValidParams()` is a no-op with a commented-out TODO
- No authentication or authorization anywhere in the application
- `@Resource` on `Customer` and `Order` exposes scaffold CRUD without any access control
- `OrderController.show()` builds a HAL link using `g.link()` inline — business logic in controller
- Inconsistent rendering strategies across controllers (4 different approaches)
- `grails.controllers.defaultScope = 'singleton'` with stateful patterns is a latent thread-safety risk
- `curl_samples.txt` uses plain `http://` URLs

## Test Coverage Gaps
- No integration tests
- No tests for `CustomerController.json()`, `CustomerController.custom()`
- No tests for `OrderController.show()` HAL link building
- No tests for `OrderXmlRenderer`
- No tests for `OrderLine` price calculation with null product
- No negative-price test for `Product` using `null` price

## Architecture Notes
- 4 rendering strategies in one app: legacy `as JSON`, builder closure, custom AbstractRenderer, HAL beans
- `resources.groovy` registers HAL renderers but HAL on individual domain objects is noted as "flaky" in README
- `UrlMappings.groovy` has a catch-all `/$controller/$action?/$id?` that bypasses `allowedMethods`
- `OrderLineController` is pure scaffold with no `allowedMethods` declared
- `static mapping = { orderLines fetch: 'join' }` on Order — eager fetch, can cause N+1 on order lists

## Files of Interest
- `/grails-app/domain/s2gx/` — all 4 domain classes
- `/grails-app/controllers/s2gx/` — 4 controllers
- `/grails-app/conf/DataSource.groovy` — DB config including prod H2 issue
- `/grails-app/conf/BuildConfig.groovy` — console plugin, Java 1.6 target
- `/src/groovy/s2gx/OrderXmlRenderer.groovy` — custom renderer with NPE risk
- `/test/unit/s2gx/` — 8 test files, 2 are stubs
