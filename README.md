# Food Ordering and Management System

Full-stack food ordering project with:
- Frontend: HTML, CSS, JavaScript, Bootstrap
- Backend: Spring Boot (Java 17, Maven)
- Database: Microsoft SQL Server (`food_system`)

## Project Structure
- `frontend/` static web pages
- `backend/smart-food-system/` Spring Boot API
- `DB/profile-&-role-management.sql` SQL schema/updates

## Prerequisites
- Java 17
- SQL Server (SQLEXPRESS or any SQL Server instance)
- SQL Server Management Studio (optional, for GUI)
- Python 3 (only for serving frontend locally)

## 1. Database Setup
1. Create a database named `food_system`.
2. Run the SQL script:
   `DB/profile-&-role-management.sql`
3. Update backend DB connection settings in:
   `backend/smart-food-system/src/main/resources/application.properties`

Current expected DB config:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=food_system;encrypt=false;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=135
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

## 2. Run Backend (Spring Boot)
From repository root:

```powershell
cd backend/smart-food-system
.\mvnw.cmd "-Dspring-boot.run.profiles=local" spring-boot:run
```

Backend base URL:
- `http://localhost:8090/api`

## 3. Run Frontend
From repository root:

```powershell
python -m http.server 5500 --directory frontend
```

Open in browser:
- `http://localhost:5500/Pages/login.html`

Local profile notes:
- Uses embedded H2 DB (file-based) so login works without SQL Server setup.
- Data is stored under `backend/smart-food-system/data/`.

## Default Login Users
The backend seeds default users on first run only when the `users` table is empty.

- Admin: `admin@urbanplate.com` / `Admin@123`
- Staff: `staff@urbanplate.com` / `Staff@123`
- Customer: `customer@example.com` / `Customer@123`

## Notes
- If users already exist, seeding is skipped.
- If backend cannot connect to SQL Server, verify:
  - SQL Server service is running
  - TCP/IP is enabled for the SQL Server instance
  - Port/instance in `spring.datasource.url` is correct
  - SQL credentials in `application.properties` are correct


