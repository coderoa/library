import java.sql.*;
import java.util.*;

public class LibrarianDAO {

    private static final String SELECT_SQL = """
            SELECT a.id, a.name, a.email, a.phone, a.status,
                   lc.barcode AS card_barcode, lc.issued_on, lc.status AS card_status
            FROM accounts a
            JOIN library_cards lc ON a.card_barcode = lc.barcode
            WHERE a.type = 'LIBRARIAN'
            """;

    public List<LibrarianAccount> getAll() {
        List<LibrarianAccount> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect();
             ResultSet rs = c.createStatement().executeQuery(SELECT_SQL)) {
            while (rs.next()) result.add(build(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public Optional<LibrarianAccount> findByEmail(String email) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(SELECT_SQL + " AND a.email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(build(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    /** Returns email → password for all librarians (used to populate login credentials map). */
    public Map<String, String> getAllCredentials() {
        Map<String, String> map = new HashMap<>();
        try (Connection c = DatabaseConnection.connect();
             ResultSet rs = c.createStatement().executeQuery(
                     "SELECT email, password FROM accounts WHERE type = 'LIBRARIAN'")) {
            while (rs.next()) map.put(rs.getString("email"), rs.getString("password"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    public void insert(LibrarianAccount librarian, String password) {
        try (Connection c = DatabaseConnection.connect()) {
            AccountDAOHelper.upsertCard(c, librarian.getLibraryCard());
            try (PreparedStatement ps = c.prepareStatement("""
                    INSERT INTO accounts(id, type, name, email, phone, password, card_barcode, status, outstanding_fine)
                    VALUES(?,?,?,?,?,?,?,?,0) ON CONFLICT DO NOTHING
                    """)) {
                ps.setString(1, librarian.getId());   ps.setString(2, "LIBRARIAN");
                ps.setString(3, librarian.getName()); ps.setString(4, librarian.getEmail());
                ps.setString(5, librarian.getPhone()); ps.setString(6, password);
                ps.setString(7, librarian.getLibraryCard().getBarcode());
                ps.setString(8, librarian.getStatus().name());
                ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateStatus(String id, AccountStatus status) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE accounts SET status=? WHERE id=?")) {
            ps.setString(1, status.name()); ps.setString(2, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private LibrarianAccount build(ResultSet rs) throws SQLException {
        LibraryCard card = new LibraryCard(rs.getString("card_barcode"),
                rs.getDate("issued_on").toLocalDate(),
                AccountStatus.valueOf(rs.getString("card_status")));
        return new LibrarianAccount(rs.getString("id"), rs.getString("name"),
                rs.getString("email"), rs.getString("phone"), null, card,
                AccountStatus.valueOf(rs.getString("status")));
    }
}
