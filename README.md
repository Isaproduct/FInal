# Final (Spring Boot)

## Tech Stack
- Java 17+
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA
- Liquibase (YAML changelogs)
- PostgreSQL
- JUnit 5 + Mockito (Unit Tests)

---

## 1) How to Run (Local)

### Requirements
- JDK 17+
- PostgreSQL
- Maven

### Configure Database
Edit `src/main/resources/application.properties` and set DB:

### properties
spring.datasource.url=jdbc:postgresql://localhost:5432/final_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml

### start
mvn clean test
mvn spring-boot:run


### 2) Database Migrations (Liquibase)

Migrations run automatically on application start.

Changelog files:

src/main/resources/db/changelog/db.changelog-master.yaml

src/main/resources/db/changelog/001-create-tables.yaml

src/main/resources/db/changelog/002-insert-test-data.yaml

src/main/resources/db/changelog/003-add-user-fields.yaml

To re-run migrations:

Drop schema (or create a new DB)

Run the application again

### 3) Postman Collection

Postman collection:

postman/Final.postman_collection.json

How to use:

Open Postman → Import

Select the collection file

Set baseUrl = http://localhost:8080


### 4) Architecture

Layered architecture:

model/ — JPA Entities

dto/ — Request/Response DTO

mapper/ — MapStruct mappers

repo/ — Spring Data JPA repositories

service/ — business logic

controller/ — REST API controllers

security/ — JWT + role-based security

Flow: Controller → Service → Repository → DB


mvn test


