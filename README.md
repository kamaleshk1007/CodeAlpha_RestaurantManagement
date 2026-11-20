# Restaurant Management System

Backend for restaurant operations: menu items, orders, tables, reservations and inventory.
Built with **Java 21, Spring Boot 4.1.2, Maven** and **MySQL 9.7**.

## What it does

Domain models (JPA/Hibernate, `ddl-auto=update` creates the schema on startup):

- `menu_items` — menu items (name, description, price, availability)
- `restaurant_tables` — tables (number, seating capacity, status)
- `reservations` — table reservations with overlap/capacity/out-of-service availability checks
- `orders` + `order_items` — placed orders with captured prices and totals
- `inventory` — ingredients with available quantity and unit
- `menu_item_ingredients` — recipes linking menu items to inventory (used for auto-deduction)

APIs:

- Place orders (validates availability, recipes, stock; deducts inventory atomically)
- Reserve tables (checks overlapping reservations, seating capacity, out-of-service status)
- Update inventory (create / update ingredient quantity)
- View the menu (all items, available-only, by id, create, update, toggle availability)
- Reporting (optional): daily sales and low-stock alerts

## How to run

Prerequisites: JDK 21, Apache Maven, MySQL 9.7.

1. Start MySQL on port 3306 and create the database:
   ```
   mysql -u root -e "CREATE DATABASE IF NOT EXISTS restaurant_management;"
   ```
   (On this Windows box the installed service would not start for this non-admin user, so
   `mysqld` was launched directly with a data dir under `%LOCALAPPDATA%\Temp\opencode\mysql-data`.)

2. Provide DB credentials through environment variables (no secrets are stored in the repo):
   ```
   set DB_USERNAME=root
   set DB_PASSWORD=
   ```

3. Build:
   ```
   mvn package
   ```

4. Run:
   ```
   java -jar target\restaurant-management-0.0.1-SNAPSHOT.jar
   ```

The app starts on `http://localhost:8080`.

## API list

Base path: `http://localhost:8080`

Menu
- `POST   /api/menu`                      Create a menu item
- `GET    /api/menu`                      List all menu items
- `GET    /api/menu/available`            List available menu items
- `GET    /api/menu/{id}`                 Get a menu item by id
- `PUT    /api/menu/{id}`                 Update a menu item
- `PATCH  /api/menu/{id}/availability?available=` Toggle availability

Tables
- `POST   /api/tables`                    Create a restaurant table
- `GET    /api/tables`                    List all tables
- `GET    /api/tables/{id}`               Get a table by id
- `PUT    /api/tables/{id}`               Update a table
- `PATCH  /api/tables/{id}/status?status=` Update table status (`AVAILABLE|OCCUPIED|OUT_OF_SERVICE`)

Reservations (availability check: overlap, capacity, out-of-service)
- `POST   /api/reservations`              Create a reservation (status `PENDING`)
- `GET    /api/reservations`              List all reservations
- `GET    /api/reservations/{id}`         Get a reservation by id
- `GET    /api/reservations/table/{tableId}` Reservations for a table
- `PATCH  /api/reservations/{id}/status`  Update reservation status

Orders (processing + automatic inventory update)
- `POST   /api/orders`                    Place an order (deducts inventory atomically)
- `GET    /api/orders`                    List all orders
- `GET    /api/orders/{id}`               Get an order by id

Inventory
- `POST   /api/inventory`                 Create an inventory item
- `GET    /api/inventory`                 List all inventory items
- `GET    /api/inventory/{id}`            Get an inventory item by id
- `PUT    /api/inventory/{id}`            Update an inventory item

Reporting (optional)
- `GET    /api/reports/daily-sales?date=2026-11-20`   Total sales for a day
- `GET    /api/reports/stock-alerts?threshold=5`      Ingredients below a quantity threshold

Request bodies are JSON. Validation failures return `400`; not found returns `404`;
duplicate/conflict/insufficient stock return `409`.

## Application properties (`src/main/resources/application.properties`)

DB credentials come from environment variables with safe defaults, so nothing secret is committed:

```
spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_management
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Notes on additions beyond the listed requirements

The provided skeleton already covered menu items, orders, tables, reservations and inventory.
The following small additions were made and are flagged here on purpose:

1. `exception/GlobalExceptionHandler.java` (new) — maps domain exceptions to correct HTTP
   status codes (`404` not found, `409` conflict/insufficient stock, `400` validation) instead of
   letting them surface as `500`.
2. Reporting endpoints (`service/ReportService.java`, `controller/ReportController.java`, plus
   repository query methods) — implements the **optional** reporting mentioned in the requirements
   (daily sales, stock alerts).
3. `application.properties` — `DB_USERNAME`/`DB_PASSWORD` read from environment variables with
   defaults; no real secrets.
4. `service/RestaurantTableService.java` — `getRestaurantTableById` made read-write
   (`@Transactional`) so the `PESSIMISTIC_WRITE` lock on `findById` can be acquired (it failed
   with `READ ONLY transaction` when inherited from the class-level read-only flag).
5. Test data (inventory + recipes) was inserted directly into MySQL for order-flow testing only.
   The requirements define no API for `menu_item_ingredients` (recipes), but order placement
   requires them, so they were seeded with SQL rather than adding an undocumented feature.

Admin access (optional in the requirements) is intentionally **not** implemented: it would require
adding Spring Security and an auth model, which is beyond the scope of the listed APIs.

## Git

Run `git init` locally only. Do **not** commit or push — the repository is empty and you can push
it to your own GitHub repo yourself. `.gitignore` excludes `target/`, `.env`, IDE files and secrets.
