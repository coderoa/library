import java.sql.*;
import java.util.*;

public class AuthorDAO {

    public List<Author> getAll() {
        if (CacheManager.has("authors")) return (List<Author>) CacheManager.get("authors");
        List<Author> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect();
             ResultSet rs = c.createStatement().executeQuery("SELECT id, name FROM authors")) {
            while (rs.next()) result.add(new Author(rs.getString("id"), rs.getString("name")));
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.put("authors", result);
        return result;
    }

    public Optional<Author> findById(String id) {
        String key = "author:" + id;
        if (CacheManager.has(key)) return (Optional<Author>) CacheManager.get(key);
        Optional<Author> result = Optional.empty();
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement("SELECT id, name FROM authors WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) result = Optional.of(new Author(rs.getString("id"), rs.getString("name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.put(key, result);
        return result;
    }

    public void insert(Author author) {
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO authors(id, name) VALUES(?,?) ON CONFLICT DO NOTHING")) {
            ps.setString(1, author.id()); ps.setString(2, author.name()); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("authors");
        CacheManager.invalidate("books");
    }

    public void delete(String id) {
        try (Connection c = DatabaseConnection.connect()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM book_authors WHERE author_id = ?")) {
                ps.setString(1, id); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM authors WHERE id = ?")) {
                ps.setString(1, id); ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        CacheManager.invalidate("authors");
        CacheManager.invalidate("author:" + id);
        CacheManager.invalidate("books");
        CacheManager.invalidate("book_items");
    }
}