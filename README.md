# Smart Food Ordering and Management System

Full-stack food ordering module with:
- Frontend: static HTML, CSS, JavaScript
- Backend: Spring Boot, Java 17, Maven, Spring Data JPA
- Database: H2 by default, SQL Server optional

## What Works
- Menu listing, search, category filter, availability filter, price range filter
- Menu create, edit, soft delete, and availability toggle
- Cart and order builder with live subtotal, tax, discount, delivery fee, and total
- Order creation with multiple items, notes, payment status, and order type
- Order list, order detail view, and controlled status transitions
- Backend validation, centralized error handling, and reusable pricing logic
- Seed data for menu items and demo orders

## Folder Structure
- `backend/smart-food-system/` Spring Boot API
- `frontend/Pages/` frontend screens
- `frontend/JS/` API client and food-ordering page logic
- `frontend/CSS/` shared styles plus food-ordering styles
- `DB/profile-&-role-management.sql` SQL Server schema and seed script
- `docs/qa-checklist.md` manual QA flow
- `IMPLEMENTATION_SUMMARY.md` short change summary
- `KNOWN_ASSUMPTIONS.md` follow-up assumptions and future improvements

## Default Runtime
The backend now defaults to a local H2 file database so the module runs without external setup.

Backend base URL:
- `http://localhost:8090/api`

H2 console:
- `http://localhost:8090/api/h2-console`

## Environment and Profiles
Default profile:
- H2 file database at `backend/smart-food-system/data/smart-food-system.mv.db`

Optional SQL Server profile:
- `backend/smart-food-system/src/main/resources/application-sqlserver.properties`

Important backend properties:
```properties
server.port=8090
server.servlet.context-path=/api
food.order.tax-rate=0.10
food.order.delivery-fee=250.00
```

## Run the Backend
From the repository root:

```powershell
cd backend/smart-food-system
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

To run with SQL Server instead of H2:

```powershell
.\mvnw.cmd "-Dspring-boot.run.profiles=sqlserver" spring-boot:run
```

## Run the Frontend
From the repository root:

```powershell
python -m http.server 5500 --directory frontend
```

Useful frontend pages:
- `http://localhost:5500/Pages/menu.html`
- `http://localhost:5500/Pages/order-create.html`
- `http://localhost:5500/Pages/orders.html`
- `http://localhost:5500/Pages/menu-management.html`

## Database Setup
H2:
- No manual setup needed
- Data is seeded automatically on first run

SQL Server:
1. Create a database named `food_system`
2. Run `DB/profile-&-role-management.sql`
3. Update `application-sqlserver.properties` if your server, port, username, or password differ
4. Start the backend with the `sqlserver` profile

## Seed Data
Automatically seeded on first backend run:
- 15 menu items across Burgers, Rice, Drinks, Desserts, Kottu, and Pizza
- 4 demo orders in `PENDING`, `PREPARING`, `COMPLETED`, and `CANCELLED`
- 3 demo users:
  - `admin@urbanplate.com` / `Admin@123`
  - `staff@urbanplate.com` / `Staff@123`
  - `customer@example.com` / `Customer@123`

## Main API Endpoints
Menu:
- `GET /api/menu`
- `GET /api/menu/{id}`
- `POST /api/menu`
- `PUT /api/menu/{id}`
- `DELETE /api/menu/{id}`
- `PATCH /api/menu/{id}/availability`
- `GET /api/menu/search?query=burger`
- `GET /api/menu/filter?category=Pizza&available=true&minPrice=1000&maxPrice=2000`

Orders:
- `GET /api/orders`
- `GET /api/orders/{id}`
- `GET /api/orders/{id}/items`
- `POST /api/orders`
- `PUT /api/orders/{id}`
- `PATCH /api/orders/{id}/status`
- `DELETE /api/orders/{id}`

Example success response:
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 5,
    "orderNumber": "ORD-20260403-001"
  }
}
```

Example error response:
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "customerName": "must not be blank"
  }
}
```

## Sample User Flow
1. Start backend and frontend
2. Open `Pages/menu.html`
3. Filter or search the seeded menu
4. Add multiple items to the cart
5. Enter customer details and submit the order
6. Open `Pages/orders.html` and confirm the new order appears
7. Open the order detail page and move the order through the allowed status flow

## Troubleshooting
- If the frontend shows connection errors, confirm the backend is running on `http://localhost:8090/api`
- If SQL Server mode fails, check the JDBC URL, credentials, and that TCP is enabled
- If seeded data does not appear, remove the H2 data files under `backend/smart-food-system/data/` and restart
- If a status update fails, verify the transition is valid: `PENDING -> CONFIRMED -> PREPARING -> READY -> COMPLETED`
