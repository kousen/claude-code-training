shopping
========

Grails shopping app demonstrated at SpringOne2GX 2014.

Demonstrates:

- static mapping for Order class
- old-style `as JSON` rendering and `render(contentType:'application/json')`
- new `@Resource` annotation with UrlMapping
- nested URL mappings
- HAL support (mostly)
- even has a RESTClient to demo HttpBuilder project

A couple of renderers (specifically, HAL renderers on domain objects rather than collections)
are being flaky, but mostly everything works.

## Domain Model

```mermaid
classDiagram
    class Customer {
        +String name
    }
    class Order {
        +String number
        +Date dateCreated
        +Date lastUpdated
        +getPrice() double
    }
    class OrderLine {
        +int quantity
        +getPrice() double
    }
    class Product {
        +String name
        +BigDecimal price
    }

    Customer "1" --> "many" Order : hasMany
    Order "many" --> "1" Customer : belongsTo
    Order "1" --> "many" OrderLine : hasMany
    OrderLine "many" --> "1" Order : belongsTo
    OrderLine "many" --> "1" Product : references
```

## REST API Routing

```mermaid
flowchart TD
    Client(["Client request"]) --> UrlMappings["UrlMappings.groovy"]

    UrlMappings -->|"/orders"| OrderController["OrderController"]
    UrlMappings -->|"/products"| ProductController["ProductController"]
    UrlMappings -->|"/customers"| CustomerController["CustomerController"]
    UrlMappings -->|"/customers/:id/orders"| OrderController
    UrlMappings -->|"/$controller/$action?/$id?"| OtherControllers["Convention-routed controllers"]

    OrderController --> OrderDomain["Order (@Resource json,xml)"]
    ProductController --> ProductDomain["Product (@Resource json)"]
    CustomerController --> CustomerDomain["Customer (@Resource json,xml)"]

    OrderDomain --> GORM[("GORM / Hibernate 4")]
    ProductDomain --> GORM
    CustomerDomain --> GORM
```

Ken Kousen
ken.kousen@kousenit.com
