import java.sql.*;
import java.sql.Date;
import java.util.*;

public class BookDAO {

    public List<Book> getAll() {
        if (CacheManager.has("books")) return (List<Book>) CacheManager.get("books");
        List<Book> books = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect()) {
            ResultSet rs;
            try {
                rs = c.createStatement().executeQuery(
                        "SELECT isbn, title, subject, publisher, publication_date, cover_image_path FROM books");
            } catch (SQLException e) {
                // Fallback for schemas without cover_image_path column
                rs = c.createStatement().executeQuery(
                        "SELECT isbn, title, subject, publisher, publication_date FROM books");
            }
            Map<String, List<Author>> authorsMap = loadAllAuthors(c);
            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String cover = null;
                try {
                    cover = rs.getString("cover_image_path");
                } catch (SQLException ignore) {
                    // column absent
                }
                books.add(new Book(isbn, rs.getString("title"), rs.getString("subject"),
                        rs.getString("publisher"), rs.getDate("publication_date").toLocalDate(),
                        authorsMap.getOrDefault(isbn, List.of()), cover));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.put("books", books);
        return books;
    }

    public Optional<Book> findByIsbn(String isbn) {
        String key = "book:" + isbn;
        if (CacheManager.has(key)) return (Optional<Book>) CacheManager.get(key);
        Optional<Book> result = Optional.empty();
        try (Connection c = DatabaseConnection.connect()) {
            PreparedStatement ps;
            try {
                ps = c.prepareStatement(
                        "SELECT isbn, title, subject, publisher, publication_date, cover_image_path FROM books WHERE isbn = ?");
            } catch (SQLException e) {
                // Fallback for schemas without cover_image_path column
                ps = c.prepareStatement(
                        "SELECT isbn, title, subject, publisher, publication_date FROM books WHERE isbn = ?");
            }
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String cover = null;
                    try {
                        cover = rs.getString("cover_image_path");
                    } catch (SQLException ignore) {
                        // missing column
                    }
                    result = Optional.of(new Book(isbn, rs.getString("title"), rs.getString("subject"),
                            rs.getString("publisher"), rs.getDate("publication_date").toLocalDate(),
                            loadAuthorsForBook(c, isbn), cover));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.put(key, result);
        return result;
    }

    public void insert(Book book) {
        try (Connection c = DatabaseConnection.connect()) {
            // Insert the basic book information. Attempt to include the cover_image_path
            // column if it exists. If the column does not exist, the statement will
            // throw and we will fall back to the legacy insert without the cover.
            boolean inserted = false;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO books(isbn, title, subject, publisher, publication_date, cover_image_path) " +
                    "VALUES(?,?,?,?,?,?) ON CONFLICT DO NOTHING")) {
                ps.setString(1, book.getIsbn());
                ps.setString(2, book.getTitle());
                ps.setString(3, book.getSubject());
                ps.setString(4, book.getPublisher());
                ps.setDate(5, Date.valueOf(book.getPublicationDate()));
                ps.setString(6, book.getCoverImagePath());
                ps.executeUpdate();
                inserted = true;
            } catch (SQLException ignore) {
                // Older schema without cover_image_path: fall back to legacy insert
            }
            if (!inserted) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO books(isbn, title, subject, publisher, publication_date) VALUES(?,?,?,?,?) ON CONFLICT DO NOTHING")) {
                    ps.setString(1, book.getIsbn());
                    ps.setString(2, book.getTitle());
                    ps.setString(3, book.getSubject());
                    ps.setString(4, book.getPublisher());
                    ps.setDate(5, Date.valueOf(book.getPublicationDate()));
                    ps.executeUpdate();
                }
            }
            // If a cover path is provided and the schema has the cover_image_path column
            // but the upsert did not insert it (due to existing row), attempt to update
            // the cover path. Wrap in try/catch in case the column does not exist.
            if (book.getCoverImagePath() != null) {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE books SET cover_image_path=? WHERE isbn=?")) {
                    ps.setString(1, book.getCoverImagePath());
                    ps.setString(2, book.getIsbn());
                    ps.executeUpdate();
                } catch (SQLException ignore) {
                    // If the column doesn't exist, ignore the exception
                }
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
        try (Connection c = DatabaseConnection.connect()) {
            // Attempt to update the cover path along with other fields if the column
            // exists. Otherwise fall back to legacy update without the cover.
            boolean updated = false;
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE books SET title=?, subject=?, publisher=?, cover_image_path=? WHERE isbn=?")) {
                ps.setString(1, book.getTitle());
                ps.setString(2, book.getSubject());
                ps.setString(3, book.getPublisher());
                ps.setString(4, book.getCoverImagePath());
                ps.setString(5, book.getIsbn());
                ps.executeUpdate();
                updated = true;
            } catch (SQLException ignore) {
                // Column may not exist; try update without cover_image_path
            }
            if (!updated) {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE books SET title=?, subject=?, publisher=? WHERE isbn=?")) {
                    ps.setString(1, book.getTitle());
                    ps.setString(2, book.getSubject());
                    ps.setString(3, book.getPublisher());
                    ps.setString(4, book.getIsbn());
                    ps.executeUpdate();
                }
            }
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