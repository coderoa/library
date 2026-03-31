public class EmailNotification extends Notification {
    private String email;

    public EmailNotification(int notificationId, java.util.Date createdOn, String content, String email) {
        super(notificationId, createdOn, content);
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean sendNotification() {
        // email sending logic will be added later
        System.out.println("Sending email to: " + email);
        System.out.println("Content: " + getContent());
        return true;
    }
}