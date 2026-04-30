import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ReservationDAO {

    private static final String SELECT_SQL = """
            SELECT br.id, br.created_on, br.status,
                   b.isbn, b.title, b.subject, b.publisher, b.publication_date,
                   a.id AS member_id, a.name AS member_name, a.email, a.phone,
                   a.status AS member_status, a.outstanding_fine,
                   lc.barcode AS card_barcode, lc.issued_on, lc.status AS card_status
            FROM book_reservations br
            JOIN books b ON br.book_isbn = b.isbn
            JOIN accounts a ON br.member_id = a.id
            JOIN library_cards lc ON a.card_barcode = lc.barcode
            """;

    public List<BookReservation> getAll() {
        return query(SELECT_SQL, ps -> {});
    }

    public List<BookReservation> getActive() {
        return query(SELECT_SQL + " WHERE br.status IN ('WAITING','PENDING_PICKUP')", ps -> {});
    }

    public List<BookReservation> getByMember(String memberId) {
        return query(SELECT_SQL + " WHERE br.member_id = ?",
                ps -> ps.setString(1, memberId));
    }

    public void insert(BookReservation reservation) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement("""
                     INSERT INTO book_reservations(id, book_isbn, member_id, created_on, status)
                     VALUES(?,?,?,?,?) ON CONFLICT DO NOTHING
                     """)) {
            ps.setString(1, reservation.getId());
            ps.setString(2, reservation.getBook().getIsbn());
            ps.setString(3, reservation.getMember().getId());
            ps.setDate(4, Date.valueOf(reservation.getCreatedOn()));
            ps.setString(5, reservation.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateStatus(String id, ReservationStatus status) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE book_reservations SET status=? WHERE id=?")) {
            ps.setString(1, status.name()); ps.setString(2, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void cancel(String id) {
        updateStatus(id, ReservationStatus.CANCELED);
    }

    public void complete(String id) {
        updateStatus(id, ReservationStatus.COMPLETED);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<BookReservation> query(String sql, Binder binder) {
        List<BookReservation> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            Map<String, List<Author>> authorsMap = BookDAO.loadAllAuthors(c);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(build(rs, authorsMap));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    private BookReservation build(ResultSet rs, Map<String, List<Author>> authorsMap) throws SQLException {
        String isbn = rs.getString("isbn");
        Book book = new Book(isbn, rs.getString("title"), rs.getString("subject"),
                rs.getString("publisher"), rs.getDate("publication_date").toLocalDate(),
                authorsMap.getOrDefault(isbn, List.of()));

        LibraryCard card = new LibraryCard(rs.getString("card_barcode"),
                rs.getDate("issued_on").toLocalDate(),
                AccountStatus.valueOf(rs.getString("card_status")));
        MemberAccount member = new MemberAccount(rs.getString("member_id"), rs.getString("member_name"),
                rs.getString("email"), rs.getString("phone"), null, card,
                AccountStatus.valueOf(rs.getString("member_status")));
        double fine = rs.getDouble("outstanding_fine");
        if (fine > 0) member.addFine(fine);

        return new BookReservation(rs.getString("id"), book, member,
                rs.getDate("created_on").toLocalDate(),
                ReservationStatus.valueOf(rs.getString("status")));
    }
}
