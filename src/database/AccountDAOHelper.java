import java.sql.*;

/** Shared helpers used by MemberDAO and LibrarianDAO. */
class AccountDAOHelper {

    static void upsertCard(Connection c, LibraryCard card) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO library_cards(barcode, issued_on, status) VALUES(?,?,?) ON CONFLICT DO NOTHING")) {
            ps.setString(1, card.getBarcode());
            ps.setDate(2, Date.valueOf(card.getIssuedOn()));
            ps.setString(3, card.getStatus().name());
            ps.executeUpdate();
        }
    }
}
