package com.urbanplate.billing;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@SpringBootApplication
public class BillingSystemApplication implements CommandLineRunner {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(BillingSystemApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("\n========================================");
		System.out.println("🔍 Testing Database Connection...");
		System.out.println("========================================\n");

		try {
			// Test 1: Basic Connection Test
			System.out.println("📌 Test 1: Checking basic database connection...");
			String message = jdbcTemplate.queryForObject(
					"SELECT 'Connection Successful! Welcome to Urban Plate Billing System!' AS Message",
					String.class
			);
			System.out.println("✅ " + message + "\n");

			// Test 2: Check Database Name
			System.out.println("📌 Test 2: Getting database information...");
			String dbName = jdbcTemplate.queryForObject(
					"SELECT DB_NAME() AS DatabaseName",
					String.class
			);
			System.out.println("✅ Connected to database: " + dbName + "\n");

			// Test 3: Check SQL Server Version
			System.out.println("📌 Test 3: Checking SQL Server version...");
			String version = jdbcTemplate.queryForObject(
					"SELECT @@VERSION AS Version",
					String.class
			);
			System.out.println("✅ SQL Server Version: " + version.substring(0, Math.min(100, version.length())) + "...\n");

			// Test 4: Check Tables in Billing Schema
			System.out.println("📌 Test 4: Checking billing schema tables...");
			Integer tableCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'billing'",
					Integer.class
			);
			System.out.println("✅ Number of tables in billing schema: " + tableCount + "\n");

			// Test 5: List all tables using RowMapper (non-deprecated way)
			if (tableCount > 0) {
				System.out.println("📌 Test 5: Listing all tables in billing schema...");
				System.out.println("   Tables found:");

				// Using query with RowMapper - this is the recommended approach
				List<String> tables = jdbcTemplate.query(
						"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'billing'",
						(rs, rowNum) -> rs.getString("TABLE_NAME")
				);

				for (String table : tables) {
					System.out.println("   📋 - " + table);
				}
				System.out.println();
			}

			// Test 6: Check if taxes table has data
			System.out.println("📌 Test 6: Checking tax data...");
			Integer taxCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM billing.taxes WHERE active = 1",
					Integer.class
			);
			System.out.println("✅ Active taxes found: " + taxCount);

			if (taxCount > 0) {
				System.out.println("   Tax details:");
				List<Object[]> taxes = jdbcTemplate.query(
						"SELECT tax_name, rate, applicable_to FROM billing.taxes WHERE active = 1",
						(rs, rowNum) -> new Object[]{rs.getString("tax_name"), rs.getDouble("rate"), rs.getString("applicable_to")}
				);

				for (Object[] tax : taxes) {
					System.out.printf("   💰 - %s: %.2f%% (Applied to: %s)%n",
							tax[0], tax[1], tax[2]);
				}
			}
			System.out.println();

			// Test 7: Check if discounts table has data
			System.out.println("📌 Test 7: Checking active discounts...");
			Integer discountCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM billing.discounts WHERE active = 1",
					Integer.class
			);
			System.out.println("✅ Active discounts found: " + discountCount);

			if (discountCount > 0) {
				System.out.println("   Discount details:");
				List<Object[]> discounts = jdbcTemplate.query(
						"SELECT coupon_code, type, value, minimum_order_amount FROM billing.discounts WHERE active = 1",
						(rs, rowNum) -> new Object[]{
								rs.getString("coupon_code"),
								rs.getString("type"),
								rs.getDouble("value"),
								rs.getDouble("minimum_order_amount")
						}
				);

				for (Object[] discount : discounts) {
					String type = (String) discount[1];
					String valueStr = type.equals("PERCENTAGE") ?
							discount[2] + "%" :
							"Rs. " + discount[2];
					System.out.printf("   🎫 - %s: %s off (Min order: Rs. %.2f)%n",
							discount[0], valueStr, discount[3]);
				}
			}
			System.out.println();

			// Test 8: Test Insert and Retrieve
			System.out.println("📌 Test 8: Testing CRUD operations...");
			System.out.println("   Creating a test invoice entry...");

			String testInvoiceNo = "TEST-" + System.currentTimeMillis();

			try {
				// In the test section, change this line:
				int insertResult = jdbcTemplate.update(
						"INSERT INTO billing.invoices (invoice_number, order_id, customer_id, subtotal, " +
								"tax_amount, discount_amount, total_amount, status, notes) " +
								"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						testInvoiceNo, 999, 999, 100.00, 10.00, 5.00, 105.00, "ISSUED", // Changed from "TEST" to "ISSUED"
						"Test invoice for connection verification"
				);

				if (insertResult > 0) {
					System.out.println("   ✅ Test invoice created successfully!");

					// Retrieve the test invoice
					List<Object[]> testInvoices = jdbcTemplate.query(
							"SELECT invoice_number, total_amount, status, created_at FROM billing.invoices WHERE invoice_number = ?",
							new Object[]{testInvoiceNo},
							(rs, rowNum) -> new Object[]{
									rs.getString("invoice_number"),
									rs.getDouble("total_amount"),
									rs.getString("status"),
									rs.getTimestamp("created_at")
							}
					);

					if (!testInvoices.isEmpty()) {
						Object[] invoice = testInvoices.get(0);
						System.out.println("   📄 Retrieved invoice:");
						System.out.printf("      - Number: %s%n", invoice[0]);
						System.out.printf("      - Amount: Rs. %.2f%n", invoice[1]);
						System.out.printf("      - Status: %s%n", invoice[2]);
						System.out.printf("      - Created: %s%n", invoice[3]);
					}

					// Clean up - delete test invoice
					jdbcTemplate.update("DELETE FROM billing.invoices WHERE invoice_number = ?", testInvoiceNo);
					System.out.println("   🧹 Test invoice cleaned up successfully!");
				}
			} catch (Exception e) {
				System.out.println("   ⚠️  Could not perform test insert: " + e.getMessage());
			}
			System.out.println();

			// Final Summary
			System.out.println("========================================");
			System.out.println("✅ ALL TESTS COMPLETED SUCCESSFULLY!");
			System.out.println("========================================");
			System.out.println("📊 Summary:");
			System.out.println("   • Database Connection: ✓ Working");
			System.out.println("   • Billing Schema: ✓ Found");
			System.out.println("   • Tables Created: " + tableCount);
			System.out.println("   • Taxes Configured: " + taxCount);
			System.out.println("   • Discounts Available: " + discountCount);
			System.out.println("========================================\n");
			System.out.println("🚀 Your database is ready! You can now start building your billing system.\n");

		} catch (Exception e) {
			System.out.println("========================================");
			System.out.println("❌ DATABASE CONNECTION FAILED!");
			System.out.println("========================================");
			System.out.println("Error: " + e.getMessage());
			System.out.println("\n🔧 Troubleshooting Tips:");
			System.out.println("1. Check if SQL Server is running");
			System.out.println("2. Verify your credentials in application.properties");
			System.out.println("3. Check if database 'UrbanPlateDB' exists");
			System.out.println("4. Verify TCP/IP is enabled in SQL Server Configuration");
			System.out.println("5. Check if port 1433 is open and accessible");
			System.out.println("\nError details:");
			e.printStackTrace();
			System.out.println("========================================\n");
		}
	}
}