# Real Repositories Implementation Instructions

- This project uses a H2 database.
- You should implement the repositories using JDBC.
- You should use Flyway migrations for schema control. They should be in the `src/main/resources/db/migration` folder.
- Use DataSource, not DrivenManager. Use HikariCP.
- Always use try-with-resources.
- SQLExceptions should always be caught and mapped to a
  com.guilherme.emobiliaria.shared.exception.PersistenceException,
  creating an appropriate message in com.guilherme.emobiliaria.shared.exception.ErrorMessage if it doesn't exist.
- The mapping logic should be put in a private `map` method that receives a ResultSet.
- Dependency Injection should be configured after creating a repository implementation.
