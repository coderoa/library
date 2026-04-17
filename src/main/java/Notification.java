import java.time.LocalDateTime;

public record Notification(String recipient, String message, LocalDateTime createdAt) {
    @Override
    public String toString() {
        return createdAt.toLocalTime().withNano(0) + " - " + recipient + ": " + message;
    }
}
