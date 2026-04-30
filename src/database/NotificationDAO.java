import java.sql.*;
import java.util.*;

public class NotificationDAO {

    public List<Notification> getAll() {
        List<Notification> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect();
             ResultSet rs = c.createStatement().executeQuery(
                     "SELECT recipient, message, created_at FROM notifications ORDER BY id")) {
            while (rs.next()) result.add(build(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public List<Notification> getByRecipient(String recipient) {
        List<Notification> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT recipient, message, created_at FROM notifications WHERE recipient = ? ORDER BY id")) {
            ps.setString(1, recipient);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(build(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public void insert(Notification notification) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO notifications(recipient, message, created_at) VALUES(?,?,?)")) {
            ps.setString(1, notification.recipient());
            ps.setString(2, notification.message());
            ps.setTimestamp(3, Timestamp.valueOf(notification.createdAt()));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Notification build(ResultSet rs) throws SQLException {
        return new Notification(rs.getString("recipient"), rs.getString("message"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}
