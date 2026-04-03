# QA Checklist

## Run
1. Start the backend:
   `cd backend/smart-food-system`
   `.\mvnw.cmd test`
   `.\mvnw.cmd spring-boot:run`
2. Start the frontend:
   `python -m http.server 5500 --directory frontend`
3. Open:
   `http://localhost:5500/Pages/menu.html`

## Screens to Test
- `Pages/menu.html`
- `Pages/order-create.html`
- `Pages/orders.html`
- `Pages/order-detail.html?id=1`
- `Pages/menu-management.html`

## Manual Flow
1. Confirm menu cards load with seeded items.
2. Search for `burger` and verify the list narrows.
3. Filter by category and price range.
4. Add multiple items to the cart.
5. Change quantities and remove one item.
6. Switch order type between `TAKEAWAY`, `DINE_IN`, and `DELIVERY`.
7. Verify table number is needed for dine-in and delivery address is needed for delivery.
8. Submit an order and confirm the success message shows the new order number.
9. Open `Pages/orders.html` and verify the new order appears.
10. Open the new order detail page and move it through valid statuses.
11. Try an invalid jump such as `PENDING -> READY` and verify the backend rejects it.
12. Open `Pages/menu-management.html`, create a menu item, edit it, toggle availability, and soft delete it.

## Sample API Calls
- `GET http://localhost:8090/api/menu`
- `GET http://localhost:8090/api/orders`
- `PATCH http://localhost:8090/api/orders/1/status`
- `POST http://localhost:8090/api/menu`

## Expected Results
- Menu API returns consistent JSON with `success`, `message`, and `data`
- Backend totals match the detail page totals
- Unavailable menu items cannot be ordered
- Invalid status transitions return an error response instead of silently changing status
- New orders persist and remain visible after page refresh
