package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V5__remove_cpf_cnpj_unique_constraints extends BaseJavaMigration {

  @Override
  public void migrate(Context context) throws Exception {
    Connection connection = context.getConnection();
    dropUniqueConstraint(connection, "PHYSICAL_PERSONS", "CPF");
    dropUniqueConstraint(connection, "JURIDICAL_PERSONS", "CNPJ");
  }

  private void dropUniqueConstraint(Connection connection, String tableName, String columnName)
      throws SQLException {
    String constraintName = findUniqueConstraintName(connection, tableName, columnName);
    if (constraintName == null || constraintName.isBlank()) {
      return;
    }

    String escapedConstraintName = constraintName.replace("\"", "\"\"");
    try (Statement statement = connection.createStatement()) {
      statement.execute(
          "ALTER TABLE " + tableName + " DROP CONSTRAINT \"" + escapedConstraintName + "\"");
    }
  }

  private String findUniqueConstraintName(Connection connection, String tableName, String columnName)
      throws SQLException {
    String sql =
        """
            SELECT tc.CONSTRAINT_NAME
            FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                     JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu
                          ON tc.CONSTRAINT_SCHEMA = ccu.CONSTRAINT_SCHEMA
                              AND tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME
            WHERE tc.TABLE_SCHEMA = 'PUBLIC'
              AND tc.TABLE_NAME = ?
              AND tc.CONSTRAINT_TYPE = 'UNIQUE'
              AND ccu.COLUMN_NAME = ?
            """;

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, tableName);
      statement.setString(2, columnName);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getString("CONSTRAINT_NAME");
        }
      }
    }
    return null;
  }
}
