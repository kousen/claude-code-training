# Shopping Service Modernization Plan

## Overview

Modernize the legacy Grails 2.4.3 shopping application (Groovy, 2014) into a modern Spring Boot 3.x application with Java 17+.

---

## 1. Framework: Grails 2.4 → Spring Boot 3.x

- `BuildConfig.groovy` → `build.gradle.kts` (or Maven `pom.xml`) with Spring Boot starters
- Embedded Tomcat 7 → Spring Boot's embedded Tomcat/Netty
- Java 1.6 target → Java 17+ (records, sealed classes, text blocks)
- H2 dev database stays, with proper Spring profiles instead of Grails environment blocks

## 2. Language: Groovy → Java (or Kotlin)

The Groovy code is straightforward — no heavy metaprogramming or AST transforms. It maps cleanly to Java.

**Trade-off:** Kotlin preserves some Groovy conciseness (data classes, null safety); Java is more universally understood.

## 3. Domain Model: GORM → JPA/Hibernate Entities

| Grails GORM | Spring Boot JPA |
|---|---|
| `static hasMany = [orders:Order]` | `@OneToMany(mappedBy = "customer")` |
| `static belongsTo = [customer:Customer]` | `@ManyToOne` + `@JoinColumn` |
| `static constraints = { name blank: false }` | `@NotBlank` (Bean Validation) |
| `static mapping = { table 'orders' }` | `@Table(name = "orders")` |
| `dateCreated` / `lastUpdated` magic | `@CreatedDate` / `@LastModifiedDate` with Spring Data Auditing |

## 4. Controllers → REST Controllers + Service Layer

Current `OrderController` mixes business logic with HTTP handling. Other controllers use `static scaffold = true`.

**Modern structure:**
```
Controller (thin)  →  Service (business logic)  →  Repository (data access)
```

- `@RestController` replaces Grails controllers
- Spring Data JPA repositories replace GORM dynamic finders
- DTOs replace direct domain serialization
- Custom `json()` and `custom()` actions become proper endpoints with response DTOs

## 5. Rendering: Mixed Approaches → Consistent JSON via Jackson

Current app has four different rendering strategies:
1. `as JSON` (legacy Grails converter)
2. `render(contentType: 'application/json')` with builder closures
3. Custom `AbstractRenderer<Order>` for XML
4. Spring bean-registered HAL renderers in `resources.groovy`

Consolidate to Jackson serialization with optional Spring HATEOAS for hypermedia links.

## 6. URL Design: Keep the Good Parts

Current mappings are well-designed and map directly to Spring's `@RequestMapping`:
```
/products              → ProductController
/customers             → CustomerController
/customers/{id}/orders → nested orders
```

## 7. Testing: Spock → JUnit 5 + Spring Boot Test

| Current | Modern |
|---|---|
| `@TestFor(ProductController)` | `@WebMvcTest(ProductController.class)` |
| `@Mock(Product)` | `@MockBean ProductRepository` |
| `mockForConstraintsTests()` | Bean Validation unit tests |
| `response.json` assertions | MockMvc + JSONPath or AssertJ |

**Note:** `CustomerControllerSpec` and `OrderLineControllerSpec` are stubs — fill these gaps during modernization.

## 8. Configuration

- `DataSource.groovy` → `application.yml` with Spring profiles (`dev`, `test`, `prod`)
- `BootStrap.groovy` seed data → `CommandLineRunner` or `data.sql`
- `Config.groovy` MIME types → Jackson configuration
- `resources.groovy` renderer beans → Jackson serializers or `@JsonView`

---

## Migration Order

1. **Set up Spring Boot project** — build file, dependencies, Java 17+
2. **Migrate domain model** — JPA entities with validation annotations
3. **Add repositories** — Spring Data JPA interfaces
4. **Create service layer** — extract business logic (price calculation, etc.)
5. **Build REST controllers** — thin controllers delegating to services
6. **Migrate seed data** — `CommandLineRunner` with same products/customers/orders
7. **Write tests** — JUnit 5 + MockMvc, filling gaps from stub tests
8. **Remove old code** — once new app passes all equivalent tests

---

## Open Decisions

| Decision | Options |
|---|---|
| **Language** | Java (widest adoption) vs. Kotlin (concise, null-safe) |
| **Build tool** | Gradle Kotlin DSL vs. Maven |
| **API style** | Plain JSON vs. HAL/HATEOAS |
| **Database** | Keep H2 for dev, or add PostgreSQL for production parity? |
| **Migration strategy** | Clean rewrite (recommended given small size) vs. incremental Grails upgrade path |
