package com.guilherme.emobiliaria.config.infrastructure.repository;

import com.guilherme.emobiliaria.config.domain.entity.Config;
import com.guilherme.emobiliaria.config.domain.repository.ConfigRepository;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcConfigRepository implements ConfigRepository {

  private final DataSource dataSource;

  @Inject
  public JdbcConfigRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Config get() {
    String sql = "SELECT id, default_landlord_id, default_landlord_type FROM configs WHERE id = 1";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      rs.next();
      return map(rs, conn);
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Config.NOT_FOUND, e);
    }
  }

  @Override
  public Config set(Config config) {
    String sql = "UPDATE configs SET default_landlord_id = ?, default_landlord_type = ? WHERE id = 1";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      if (config.getDefaultLandlord() == null) {
        stmt.setNull(1, java.sql.Types.BIGINT);
        stmt.setNull(2, java.sql.Types.VARCHAR);
      } else {
        stmt.setLong(1, config.getDefaultLandlord().getId());
        stmt.setString(2, personType(config.getDefaultLandlord()));
      }
      stmt.executeUpdate();
      return config;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Config.NOT_FOUND, e);
    }
  }

  private Config map(ResultSet rs, Connection conn) throws SQLException {
    long id = rs.getLong("id");
    long landlordId = rs.getLong("default_landlord_id");
    String landlordType = rs.getString("default_landlord_type");

    Person defaultLandlord = null;
    if (!rs.wasNull() && landlordType != null) {
      defaultLandlord = loadPerson(conn, landlordId, landlordType);
    }

    return Config.restore(id, defaultLandlord);
  }

  private Person loadPerson(Connection conn, long id, String type) throws SQLException {
    if ("PHYSICAL".equals(type)) {
      return loadPhysicalPerson(conn, id);
    }
    return loadJuridicalPerson(conn, id);
  }

  private PhysicalPerson loadPhysicalPerson(Connection conn, long id) throws SQLException {
    String sql = "SELECT id, name, nationality, civil_state, occupation, cpf, id_card_number, address_id FROM physical_persons WHERE id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        Address address = loadAddress(conn, rs.getLong("address_id"));
        return PhysicalPerson.restore(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("nationality"),
            CivilState.valueOf(rs.getString("civil_state")),
            rs.getString("occupation"),
            rs.getString("cpf"),
            rs.getString("id_card_number"),
            address
        );
      }
    }
  }

  private JuridicalPerson loadJuridicalPerson(Connection conn, long id) throws SQLException {
    String sql = "SELECT id, corporate_name, cnpj, address_id FROM juridical_persons WHERE id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        List<PhysicalPerson> representatives = loadRepresentatives(conn, rs.getLong("id"));
        Address address = loadAddress(conn, rs.getLong("address_id"));
        return JuridicalPerson.restore(
            rs.getLong("id"),
            rs.getString("corporate_name"),
            rs.getString("cnpj"),
            representatives,
            address
        );
      }
    }
  }

  private List<PhysicalPerson> loadRepresentatives(Connection conn, long juridicalPersonId) throws SQLException {
    String sql = "SELECT physical_person_id FROM juridical_person_representatives WHERE juridical_person_id=?";
    List<PhysicalPerson> representatives = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, juridicalPersonId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          representatives.add(loadPhysicalPerson(conn, rs.getLong("physical_person_id")));
        }
      }
    }
    return representatives;
  }

  private Address loadAddress(Connection conn, long id) throws SQLException {
    String sql = "SELECT id, cep, address, number, complement, neighborhood, city, state FROM addresses WHERE id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        return Address.restore(
            rs.getLong("id"),
            rs.getString("cep"),
            rs.getString("address"),
            rs.getString("number"),
            rs.getString("complement"),
            rs.getString("neighborhood"),
            rs.getString("city"),
            BrazilianState.valueOf(rs.getString("state"))
        );
      }
    }
  }

  private String personType(Person person) {
    return person instanceof PhysicalPerson ? "PHYSICAL" : "JURIDICAL";
  }
}
