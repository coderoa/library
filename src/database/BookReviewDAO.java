import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookReviewDAO {

    public boolean addReview(String bookIsbn, String memberId, int rating, String comment) {
        String sql = "INSERT INTO book_reviews (book_isbn, member_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookIsbn);
            stmt.setString(2, memberId);
            stmt.setInt(3, rating);
            stmt.setString(4, comment);
            stmt.setString(5, LocalDateTime.now().toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String[]> getReviewsByBook(String bookIsbn) {
        List<String[]> reviews = new ArrayList<>();
        String sql = """
            SELECT r.id, a.name, r.rating, r.comment, r.created_at, r.member_id
            FROM book_reviews r
            JOIN accounts a ON r.member_id = a.id
            WHERE r.book_isbn = ?
            ORDER BY r.created_at DESC
            """;
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookIsbn);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reviews.add(new String[]{
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("rating"),
                    rs.getString("comment"),
                    rs.getString("created_at"),
                    rs.getString("member_id")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public boolean deleteReview(int reviewId) {
        String sql = "DELETE FROM book_reviews WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasReviewed(String bookIsbn, String memberId) {
        String sql = "SELECT 1 FROM book_reviews WHERE book_isbn = ? AND member_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookIsbn);
            stmt.setString(2, memberId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getAverageRating(String bookIsbn) {
        String sql = "SELECT AVG(rating) AS avg_rating FROM book_reviews WHERE book_isbn = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookIsbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("avg_rating");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
