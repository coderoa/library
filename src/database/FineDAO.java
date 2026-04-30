import java.sql.*;
import java.util.*;

public class FineDAO {

    public List<Fine> getAll() {
        List<Fine> result = new ArrayList<>();
        String sql = """
                SELECT f.member_id, a.name AS member_name, f.book_title, f.late_days, f.amount
                FROM fines f
                JOIN accounts a ON f.member_id = a.id
                ORDER BY f.id
                """;
        try (Connection c = DatabaseConnection.connect();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) result.add(build(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public List<Fine> getByMember(String memberId) {
        List<Fine> result = new ArrayList<>();
        String sql = """
                SELECT f.member_id, a.name AS member_name, f.book_title, f.late_days, f.amount
                FROM fines f
                JOIN accounts a ON f.member_id = a.id
                WHERE f.member_id = ?
                ORDER BY f.id
                """;
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(build(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public void insert(Fine fine) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO fines(member_id, book_title, late_days, amount) VALUES(?,?,?,?)")) {
            ps.setString(1, fine.memberId());
            ps.setString(2, fine.bookTitle());
            ps.setLong(3, fine.lateDays());
            ps.setDouble(4, fine.amount());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Called when a member pays their fines — removes fine records for that member. */
    public void deleteByMember(String memberId) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM fines WHERE member_id = ?")) {
            ps.setString(1, memberId); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Fine build(ResultSet rs) throws SQLException {
        return new Fine(rs.getString("member_id"), rs.getString("member_name"),
                rs.getString("book_title"), rs.getLong("late_days"), rs.getDouble("amount"));
    }
}
