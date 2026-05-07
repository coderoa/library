import java.sql.*;
import java.sql.Date;
import java.util.*;

public class LendingDAO {

    private static final String SELECT_SQL = """
            SELECT bl.id, bl.checkout_date, bl.due_date, bl.return_date,
                   bi.barcode AS item_barcode, bi.status AS item_status, bi.due_date AS item_due,
                   bi.format, bi.is_reference_only, bi.rack_number, r.location_identifier,
                   b.isbn, b.title, b.subject, b.publisher, b.publication_date,
                   a.id AS member_id, a.name AS member_name, a.email, a.phone,
                   a.status AS member_status, a.outstanding_fine,
                   lc.barcode AS card_barcode, lc.issued_on, lc.status AS card_status
            FROM book_lendings bl
            JOIN book_items bi ON bl.book_item_barcode = bi.barcode
            JOIN books b ON bi.book_isbn = b.isbn
            LEFT JOIN racks r ON bi.rack_number = r.rack_number
            JOIN accounts a ON bl.member_id = a.id
            JOIN library_cards lc ON a.card_barcode = lc.barcode
            """;

    public List<BookLending> getAll() {
        if (CacheManager.has("lendings")) return (List<BookLending>) CacheManager.get("lendings");
        List<BookLending> result = query(SELECT_SQL, ps -> {});
        CacheManager.put("lendings", result);
        return result;
    }

    public List<BookLending> getActive() {
        if (CacheManager.has("lendings_active")) return (List<BookLending>) CacheManager.get("lendings_active");
        List<BookLending> result = query(SELECT_SQL + " WHERE bl.return_date IS NULL", ps -> {});
        CacheManager.put("lendings_active", result);
        return result;
    }

    public List<BookLending> getByMember(String memberId) {
        String key = "lendings_member:" + memberId;
        if (CacheManager.has(key)) return (List<BookLending>) CacheManager.get(key);
        List<BookLending> result = query(SELECT_SQL + " WHERE bl.member_id = ?",
                ps -> ps.setString(1, memberId));
        CacheManager.put(key, result);
        return result;
    }

    /** Full history for a member (active and returned). */
    public List<BookLending> getHistoryByMember(String memberId) {
        return getByMember(memberId);
    }

    public void insert(BookLending lending) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement("""
                     INSERT INTO book_lendings(id, book_item_barcode, member_id, checkout_date, due_date, return_date)
                     VALUES(?,?,?,?,?,?) ON CONFLICT DO NOTHING
                     """)) {
            ps.setString(1, lending.getId());
            ps.setString(2, lending.getBookItem().getBarcode());
            ps.setString(3, lending.getMember().getId());
            ps.setDate(4, Date.valueOf(lending.getCheckoutDate()));
            ps.setDate(5, Date.valueOf(lending.getDueDate()));
            ps.setObject(6, lending.getReturnDate() != null ? Date.valueOf(lending.getReturnDate()) : null);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("lendings");
        CacheManager.invalidate("lendings_active");
        CacheManager.invalidate("lendings_member:" + lending.getMember().getId());
        CacheManager.invalidate("book_items");
        CacheManager.invalidate("book_item:" + lending.getBookItem().getBarcode());
    }

    public void updateReturn(BookLending lending) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE book_lendings SET return_date=? WHERE id=?")) {
            ps.setObject(1, lending.getReturnDate() != null ? Date.valueOf(lending.getReturnDate()) : null);
            ps.setString(2, lending.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("lendings");
        CacheManager.invalidate("lendings_active");
        CacheManager.invalidate("lendings_member:" + lending.getMember().getId());
    }

    public void deleteByMember(String memberId) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM book_lendings WHERE member_id=?")) {
            ps.setString(1, memberId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("lendings");
        CacheManager.invalidate("lendings_active");
        CacheManager.invalidateByPrefix("lendings_member:");
    }

    public void updateDueDate(BookLending lending) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE book_lendings SET due_date=? WHERE id=?")) {
            ps.setDate(1, Date.valueOf(lending.getDueDate()));
            ps.setString(2, lending.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("lendings");
        CacheManager.invalidate("lendings_active");
        CacheManager.invalidate("lendings_member:" + lending.getMember().getId());
    }

    // ── helpers ───────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<BookLending> query(String sql, Binder binder) {
        List<BookLending> result = new ArrayList<>();
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

    private BookLending build(ResultSet rs, Map<String, List<Author>> authorsMap) throws SQLException {
        String isbn = rs.getString("isbn");
        Book book = new Book(isbn, rs.getString("title"), rs.getString("subject"),
                rs.getString("publisher"), rs.getDate("publication_date").toLocalDate(),
                authorsMap.getOrDefault(isbn, List.of()));
        Rack rack = new Rack(rs.getString("rack_number"), rs.getString("location_identifier"));
        BookItem item = new BookItem(rs.getString("item_barcode"), book, rack,
                BookStatus.valueOf(rs.getString("item_status")),
                BookFormat.valueOf(rs.getString("format")),
                rs.getBoolean("is_reference_only"));
        Date itemDue = rs.getDate("item_due");
        if (itemDue != null) item.setDueDate(itemDue.toLocalDate());

        LibraryCard card = new LibraryCard(rs.getString("card_barcode"),
                rs.getDate("issued_on").toLocalDate(),
                AccountStatus.valueOf(rs.getString("card_status")));
        MemberAccount member = new MemberAccount(rs.getString("member_id"), rs.getString("member_name"),
                rs.getString("email"), rs.getString("phone"), null, card,
                AccountStatus.valueOf(rs.getString("member_status")));
        double fine = rs.getDouble("outstanding_fine");
        if (fine > 0) member.addFine(fine);

        BookLending lending = new BookLending(rs.getString("id"), item, member,
                rs.getDate("checkout_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate());
        Date ret = rs.getDate("return_date");
        if (ret != null) lending.setReturnDate(ret.toLocalDate());
        return lending;
    }
}
