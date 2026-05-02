import java.sql.*;
import java.sql.Date;
import java.util.*;

public class BookDAO {

    public List<Book> getAll() {
        if (CacheManager.has("books")) return (List<Book>) CacheManager.get("books");
        List<Book> books = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect();
             ResultSet rs = c.createStatement().executeQuery(
                     "SELECT isbn, title, subject, publisher, publication_date FROM books")) {
            Map<String, List<Author>> authorsMap = loadAllAuthors(c);
            while (rs.next()) {
                String isbn = rs.getString("isbn");
                books.add(new Book(isbn, rs.getString("title"), rs.getString("subject"),
                        rs.getString("publisher"), rs.getDate("publication_date").toLocalDate(),
                        authorsMap.getOrDefault(isbn, List.of())));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.put("books", books);
        return books;
    }

    public Optional<Book> findByIsbn(String isbn) {
        String key = "book:" + isbn;
        if (CacheManager.has(key)) return (Optional<Book>) CacheManager.get(key);
        Optional<Book> result = Optional.empty();
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT isbn, title, subject, publisher, publication_date FROM books WHERE isbn = ?")) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    result = Optional.of(new Book(isbn, rs.getString("title"), rs.getString("subject"),
                            rs.getString("publisher"), rs.getDate("publication_date").toLocalDate(),
                            loadAuthorsForBook(c, isbn)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.put(key, result);
        return result;
    }

    public void insert(Book book) {
        try (Connection c = DatabaseConnection.connect()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO books(isbn, title, subject, publisher, publication_date) VALUES(?,?,?,?,?) ON CONFLICT DO NOTHING")) {
                ps.setString(1, book.getIsbn());
                ps.setString(2, book.getTitle());
                ps.setString(3, book.getSubject());
                ps.setString(4, book.getPublisher());
                ps.setDate(5, Date.valueOf(book.getPublicationDate()));
                ps.executeUpdate();
            }
            for (Author a : book.getAuthors()) {
                upsertAuthor(c, a);
                linkBookAuthor(c, book.getIsbn(), a.id());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("books");
        CacheManager.invalidate("book_items");
    }

    public void update(Book book) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE books SET title=?, subject=?, publisher=? WHERE isbn=?")) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getSubject());
            ps.setString(3, book.getPublisher());
            ps.setString(4, book.getIsbn());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("books");
        CacheManager.invalidate("book:" + book.getIsbn());
        CacheManager.invalidate("book_items");
    }

    public void delete(String isbn) {
        try (Connection c = DatabaseConnection.connect()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM book_authors WHERE book_isbn = ?")) {
                ps.setString(1, isbn); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM books WHERE isbn = ?")) {
                ps.setString(1, isbn); ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("books");
        CacheManager.invalidate("book:" + isbn);
        CacheManager.invalidate("book_items");
        CacheManager.invalidate("lendings");
        CacheManager.invalidate("lendings_active");
        CacheManager.invalidate("reservations");
        CacheManager.invalidate("reservations_active");
    }

    // ── package-visible helpers reused by BookItemDAO / LendingDAO ────────

    static void upsertAuthor(Connection c, Author a) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO authors(id, name) VALUES(?,?) ON CONFLICT DO NOTHING")) {
            ps.setString(1, a.id()); ps.setString(2, a.name()); ps.executeUpdate();
        }
    }

    static void linkBookAuthor(Connection c, String isbn, String authorId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO book_authors(book_isbn, author_id) VALUES(?,?) ON CONFLICT DO NOTHING")) {
            ps.setString(1, isbn); ps.setString(2, authorId); ps.executeUpdate();
        }
    }

    static Map<String, List<Author>> loadAllAuthors(Connection c) throws SQLException {
        Map<String, Author> byId = new HashMap<>();
        try (ResultSet rs = c.createStatement().executeQuery("SELECT id, name FROM authors")) {
            while (rs.next())
                byId.put(rs.getString("id"), new Author(rs.getString("id"), rs.getString("name")));
        }
        Map<String, List<Author>> result = new HashMap<>();
        try (ResultSet rs = c.createStatement().executeQuery(
                "SELECT book_isbn, author_id FROM book_authors")) {
            while (rs.next()) {
                Author a = byId.get(rs.getString("author_id"));
                if (a != null)
                    result.computeIfAbsent(rs.getString("book_isbn"), k -> new ArrayList<>()).add(a);
            }
        }
        return result;
    }

    static List<Author> loadAuthorsForBook(Connection c, String isbn) throws SQLException {
        List<Author> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT a.id, a.name FROM authors a JOIN book_authors ba ON a.id = ba.author_id WHERE ba.book_isbn = ?")) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Author(rs.getString("id"), rs.getString("name")));
            }
        }
        return list;
    }
}