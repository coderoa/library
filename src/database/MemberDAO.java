import java.sql.*;
import java.util.*;

public class MemberDAO {

    private static final String SELECT_SQL = """
            SELECT a.id, a.name, a.email, a.phone, a.status, a.outstanding_fine,
                   lc.barcode AS card_barcode, lc.issued_on, lc.status AS card_status
            FROM accounts a
            JOIN library_cards lc ON a.card_barcode = lc.barcode
            WHERE a.type = 'MEMBER'
            """;

    public List<MemberAccount> getAll() {
        if (CacheManager.has("members")) return (List<MemberAccount>) CacheManager.get("members");
        List<MemberAccount> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect();
             ResultSet rs = c.createStatement().executeQuery(SELECT_SQL)) {
            while (rs.next()) result.add(build(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.put("members", result);
        return result;
    }

    public Optional<MemberAccount> findByEmail(String email) {
        String key = "member:" + email;
        if (CacheManager.has(key)) return (Optional<MemberAccount>) CacheManager.get(key);
        Optional<MemberAccount> result = Optional.empty();
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(SELECT_SQL + " AND a.email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) result = Optional.of(build(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.put(key, result);
        return result;
    }

    /** Returns email → password for all members (used to populate login credentials map). */
    public Map<String, String> getAllCredentials() {
        if (CacheManager.has("member_credentials")) return (Map<String, String>) CacheManager.get("member_credentials");
        Map<String, String> map = new HashMap<>();
        try (Connection c = DatabaseConnection.connect();
             ResultSet rs = c.createStatement().executeQuery(
                     "SELECT email, password FROM accounts WHERE type = 'MEMBER'")) {
            while (rs.next()) map.put(rs.getString("email"), rs.getString("password"));
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.put("member_credentials", map);
        return map;
    }

    public void insert(MemberAccount member, String password) {
        try (Connection c = DatabaseConnection.connect()) {
            AccountDAOHelper.upsertCard(c, member.getLibraryCard());
            try (PreparedStatement ps = c.prepareStatement("""
                    INSERT INTO accounts(id, type, name, email, phone, password, card_barcode, status, outstanding_fine)
                    VALUES(?,?,?,?,?,?,?,?,?) ON CONFLICT DO NOTHING
                    """)) {
                ps.setString(1, member.getId());   ps.setString(2, "MEMBER");
                ps.setString(3, member.getName()); ps.setString(4, member.getEmail());
                ps.setString(5, member.getPhone()); ps.setString(6, password);
                ps.setString(7, member.getLibraryCard().getBarcode());
                ps.setString(8, member.getStatus().name());
                ps.setDouble(9, member.getOutstandingFine());
                ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("members");
        CacheManager.invalidate("member_credentials");
    }

    public void updateStatus(String id, AccountStatus status) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE accounts SET status=? WHERE id=?")) {
            ps.setString(1, status.name()); ps.setString(2, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("members");
        CacheManager.invalidate("lendings");
        CacheManager.invalidate("lendings_active");
        CacheManager.invalidate("reservations");
        CacheManager.invalidate("reservations_active");
        CacheManager.invalidate("fines");
    }

    public void updateFine(String id, double fine) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE accounts SET outstanding_fine=? WHERE id=?")) {
            ps.setDouble(1, fine); ps.setString(2, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("members");
        CacheManager.invalidate("lendings");
        CacheManager.invalidate("lendings_active");
        CacheManager.invalidate("fines");
    }

    public void delete(String id) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM accounts WHERE id=?")) {
            ps.setString(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("members");
        CacheManager.invalidate("member_credentials");
        CacheManager.invalidate("lendings");
        CacheManager.invalidate("lendings_active");
        CacheManager.invalidate("reservations");
        CacheManager.invalidate("reservations_active");
        CacheManager.invalidate("fines");
    }

    private MemberAccount build(ResultSet rs) throws SQLException {
        LibraryCard card = new LibraryCard(rs.getString("card_barcode"),
                rs.getDate("issued_on").toLocalDate(),
                AccountStatus.valueOf(rs.getString("card_status")));
        MemberAccount m = new MemberAccount(rs.getString("id"), rs.getString("name"),
                rs.getString("email"), rs.getString("phone"), null, card,
                AccountStatus.valueOf(rs.getString("status")));
        double fine = rs.getDouble("outstanding_fine");
        if (fine > 0) m.addFine(fine);
        return m;
    }
}