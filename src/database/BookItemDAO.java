import java.sql.*;
import java.sql.Date;
import java.util.*;

public class BookItemDAO {

    private static final String SELECT_SQL = """
            SELECT bi.barcode, bi.status, bi.due_date, bi.format, bi.is_reference_only,
                   bi.rack_number, r.location_identifier,
                   b.isbn, b.title, b.subject, b.publisher, b.publication_date
            FROM book_items bi
            JOIN books b ON bi.book_isbn = b.isbn
            LEFT JOIN racks r ON bi.rack_number = r.rack_number
            """;

    public List<BookItem> getAll() {
        if (CacheManager.has("book_items")) return (List<BookItem>) CacheManager.get("book_items");
        List<BookItem> items = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect();
             ResultSet rs = c.createStatement().executeQuery(SELECT_SQL)) {
            Map<String, List<Author>> authorsMap = BookDAO.loadAllAuthors(c);
            while (rs.next()) items.add(build(rs, authorsMap));
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.put("book_items", items);
        return items;
    }

    public Optional<BookItem> findByBarcode(String barcode) {
        String key = "book_item:" + barcode;
        if (CacheManager.has(key)) return (Optional<BookItem>) CacheManager.get(key);
        Optional<BookItem> result = Optional.empty();
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(SELECT_SQL + " WHERE bi.barcode = ?")) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String isbn = rs.getString("isbn");
                    result = Optional.of(build(rs, Map.of(isbn, BookDAO.loadAuthorsForBook(c, isbn))));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.put(key, result);
        return result;
    }

    public void insert(BookItem item) {
        try (Connection c = DatabaseConnection.connect()) {
            ensureBook(c, item.getBook());
            ensureRack(c, item.getRack());
            try (PreparedStatement ps = c.prepareStatement("""
                    INSERT INTO book_items(barcode, book_isbn, rack_number, status, due_date, format, is_reference_only)
                    VALUES(?,?,?,?,?,?,?) ON CONFLICT DO NOTHING
                    """)) {
                ps.setString(1, item.getBarcode());
                ps.setString(2, item.getBook().getIsbn());
                ps.setString(3, item.getRack().rackNumber());
                ps.setString(4, item.getStatus().name());
                ps.setObject(5, item.getDueDate());
                ps.setString(6, item.getFormat().name());
                ps.setBoolean(7, item.isReferenceOnly());
                ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("book_items");
        CacheManager.invalidate("books");
    }

    public void updateStatus(BookItem item) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE book_items SET status=?, due_date=? WHERE barcode=?")) {
            ps.setString(1, item.getStatus().name());
            ps.setObject(2, item.getDueDate());
            ps.setString(3, item.getBarcode());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("book_items");
        CacheManager.invalidate("book_item:" + item.getBarcode());
        CacheManager.invalidate("lendings");
        CacheManager.invalidate("lendings_active");
    }

    public void delete(String barcode) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM book_items WHERE barcode = ?")) {
            ps.setString(1, barcode); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("book_items");
        CacheManager.invalidate("book_item:" + barcode);
        CacheManager.invalidate("lendings");
        CacheManager.invalidate("lendings_active");
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private BookItem build(ResultSet rs, Map<String, List<Author>> authorsMap) throws SQLException {
        String isbn = rs.getString("isbn");
        Book book = new Book(isbn, rs.getString("title"), rs.getString("subject"),
                rs.getString("publisher"), rs.getDate("publication_date").toLocalDate(),
                authorsMap.getOrDefault(isbn, List.of()));
        Rack rack = new Rack(rs.getString("rack_number"), rs.getString("location_identifier"));
        BookItem item = new BookItem(rs.getString("barcode"), book, rack,
                BookStatus.valueOf(rs.getString("status")),
                BookFormat.valueOf(rs.getString("format")),
                rs.getBoolean("is_reference_only"));
        Date due = rs.getDate("due_date");
        if (due != null) item.setDueDate(due.toLocalDate());
        return item;
    }

    private void ensureBook(Connection c, Book book) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO books(isbn, title, subject, publisher, publication_date) VALUES(?,?,?,?,?) ON CONFLICT DO NOTHING")) {
            ps.setString(1, book.getIsbn()); ps.setString(2, book.getTitle());
            ps.setString(3, book.getSubject()); ps.setString(4, book.getPublisher());
            ps.setDate(5, Date.valueOf(book.getPublicationDate())); ps.executeUpdate();
        }
        for (Author a : book.getAuthors()) {
            BookDAO.upsertAuthor(c, a);
            BookDAO.linkBookAuthor(c, book.getIsbn(), a.id());
        }
    }

    private void ensureRack(Connection c, Rack rack) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO racks(rack_number, location_identifier) VALUES(?,?) ON CONFLICT DO NOTHING")) {
            ps.setString(1, rack.rackNumber()); ps.setString(2, rack.locationIdentifier());
            ps.executeUpdate();
        }
    }
}