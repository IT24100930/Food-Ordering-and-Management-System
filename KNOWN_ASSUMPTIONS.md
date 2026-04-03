# Known Assumptions and Future TODOs

- The frontend is static HTML/JS, so route protection is not enforced client-side beyond navigation choices.
- Inventory reduction is intentionally implemented as a service hook and only deducts stock when tracked stock is greater than zero.
- Completed and cancelled orders are blocked from full order edits.
- The default backend runtime uses H2 for fast local verification; SQL Server remains optional through the `sqlserver` profile.
- The current pricing engine supports direct discount amounts. Percentage coupons and advanced promotions are future enhancements.
- Order status history timestamps are not stored yet; only the current status is persisted.
- Existing user/trust-score pages were preserved, but the new food-ordering pages are the main end-to-end flow for this module.
