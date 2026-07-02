# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

`shopping` is a Grails 2.4.3 / Groovy REST service (demonstrated at SpringOne2GX 2014) exposing a
Customer → Order → OrderLine → Product domain over JSON/XML/HAL. It's used in this training repo as a
legacy-modernization exercise. See README.md for architecture diagrams (Mermaid) and known-flaky areas.

## Commands

All commands run through the Grails wrapper (`./grailsw`), which downloads Grails 2.4.3 into `wrapper/` on
first use — no separately installed Grails is required.

- Run the app: `./grailsw run-app` (serves at `http://localhost:8080/shopping`)
- Run all unit tests: `./grailsw test-app`
- Run a single test: `./grailsw test-app s2gx.OrderSpec` (class name only, no package-path form)
- Run a single test method: `./grailsw test-app s2gx.OrderSpec.methodName`
- Compile: `./grailsw compile`
- Build a WAR: `./grailsw war`
- Open the Grails console (from the `console` plugin): `./grailsw console`

Tests are Spock specifications under `test/unit/s2gx/`, one per controller and one per domain class.

## Architecture

**Domain graph** (`grails-app/domain/s2gx/`): `Customer` `hasMany` `Order` (`belongsTo` `Customer`); `Order`
`hasMany` `OrderLine` (`belongsTo` `Order`); `OrderLine` references `Product`. `Order.price` and
`OrderLine.price` are computed properties (not persisted columns) derived by summing/multiplying at read
time — don't expect a `price` column on `orders` beyond what `orderLines fetch: 'join'` eagerly loads.

**Routing** (`grails-app/conf/UrlMappings.groovy`): `/orders`, `/products`, `/customers` are declared as
REST `resources`, with `/customers/:id/orders` nested under customers. Falls through to the conventional
`/$controller/$action?/$id?(.$format)?` mapping for anything else. Controllers are thin — CRUD only, no
service layer — and rely on `@Resource(formats: [...])` on the domain classes plus Grails' `respond()` to
do content negotiation.

**Custom rendering** (`grails-app/conf/spring/resources.groovy`, `src/groovy/s2gx/OrderXmlRenderer.groovy`):
Renderers are wired by hand as Spring beans, not left to Grails defaults, because the exercise intentionally
mixes rendering styles:
- `Product` gets a plain `JsonRenderer`/`JsonCollectionRenderer` (excluding `class`) plus separate HAL JSON
  and HAL XML renderers.
- `Order` XML is rendered by a **custom** `AbstractRenderer` (`OrderXmlRenderer`) that hand-builds XML with
  `MarkupBuilder` — it is not derived from the domain class shape, so if `Order`'s fields change, this
  renderer must be updated by hand too.
- `Customer` collections get a HAL renderer registered against a custom mime type
  (`application/vnd.s2gx.cust+json`).
- The README notes HAL rendering of *individual* domain objects (as opposed to collections) is flaky —
  don't assume single-resource HAL requests work correctly.

**Data**: H2 in-memory database (`grails-app/conf/DataSource.groovy`), recreated on every restart
(`dbCreate = "create-drop"` in dev). `BootStrap.groovy` seeds baseball/football/basketball products, one
customer ("Charlie Brown"), and two orders — useful as example data when testing endpoints manually.

**Manual API examples**: `curl_samples.txt` at the repo root has copy-pasteable `curl` commands (JSON, XML,
`.format` suffix vs. `Accept` header, POST/PUT/DELETE, nested customer→orders) — use these as the reference
for expected request/response shapes rather than re-deriving them from the controllers.

**Java/Groovy REST client demo**: `src/groovy/s2gx/client_demo.groovy` shows calling this API from Groovy
using the `http-builder` library (declared in `BuildConfig.groovy`), separate from the app runtime itself.

## Notes

- Target/source compatibility is Java 1.6 (`grails.project.target.level`/`source.level` in
  `grails-app/conf/BuildConfig.groovy`) — this is a deliberately dated legacy target for the exercise, not a
  current recommendation.
- Plugins/dependencies (scaffolding, cache, asset-pipeline, hibernate4, database-migration, jquery, console)
  are all declared in `BuildConfig.groovy`'s Ivy/Maven-style DSL, not Gradle.
