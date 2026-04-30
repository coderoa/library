import java.sql.*;
import java.util.*;

public class RackDAO {

    public List<Rack> getAll() {
        List<Rack> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect();
             ResultSet rs = c.createStatement().executeQuery(
                     "SELECT rack_number, location_identifier FROM racks")) {
            while (rs.next())
                result.add(new Rack(rs.getString("rack_number"), rs.getString("location_identifier")));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public void insert(Rack rack) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO racks(rack_number, location_identifier) VALUES(?,?) ON CONFLICT DO NOTHING")) {
            ps.setString(1, rack.rackNumber()); ps.setString(2, rack.locationIdentifier());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(String rackNumber) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM racks WHERE rack_number = ?")) {
            ps.setString(1, rackNumber); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
