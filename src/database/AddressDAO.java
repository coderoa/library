import java.sql.*;
import java.util.Optional;

public class AddressDAO {

    public Optional<Address> findById(int id) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT street, city, state, postal_code, country FROM addresses WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(build(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    /** Inserts an address and returns the generated id (-1 on failure). */
    public int insert(Address address) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO addresses(street, city, state, postal_code, country) VALUES(?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, address.street());
            ps.setString(2, address.city());
            ps.setString(3, address.state());
            ps.setString(4, address.postalCode());
            ps.setString(5, address.country());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) { e.printStackTrace(); return -1; }
    }

    public void update(int id, Address address) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE addresses SET street=?, city=?, state=?, postal_code=?, country=? WHERE id=?")) {
            ps.setString(1, address.street());
            ps.setString(2, address.city());
            ps.setString(3, address.state());
            ps.setString(4, address.postalCode());
            ps.setString(5, address.country());
            ps.setInt(6, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Address build(ResultSet rs) throws SQLException {
        return new Address(rs.getString("street"), rs.getString("city"),
                rs.getString("state"), rs.getString("postal_code"), rs.getString("country"));
    }
}
