import java.sql.*;
import java.sql.Date;
import java.util.*;

public class LibraryCardDAO {

    public List<LibraryCard> getAll() {
        List<LibraryCard> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect();
             ResultSet rs = c.createStatement().executeQuery(
                     "SELECT barcode, issued_on, status FROM library_cards")) {
            while (rs.next()) result.add(build(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public Optional<LibraryCard> findByBarcode(String barcode) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT barcode, issued_on, status FROM library_cards WHERE barcode = ?")) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(build(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    public void insert(LibraryCard card) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO library_cards(barcode, issued_on, status) VALUES(?,?,?) ON CONFLICT DO NOTHING")) {
            ps.setString(1, card.getBarcode());
            ps.setDate(2, Date.valueOf(card.getIssuedOn()));
            ps.setString(3, card.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(String barcode) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM library_cards WHERE barcode=?")) {
            ps.setString(1, barcode);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateStatus(String barcode, AccountStatus status) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE library_cards SET status=? WHERE barcode=?")) {
            ps.setString(1, status.name()); ps.setString(2, barcode); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private LibraryCard build(ResultSet rs) throws SQLException {
        return new LibraryCard(rs.getString("barcode"),
                rs.getDate("issued_on").toLocalDate(),
                AccountStatus.valueOf(rs.getString("status")));
    }
}
