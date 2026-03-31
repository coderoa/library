import java.util.Date;

public abstract class Notification {
    private int notificationId;
    private Date createdOn;
    private String content;

    //Constructor
    public Notification(int notificationId, Date createdOn, String content) {
        this.notificationId = notificationId;
        this.createdOn = createdOn;
        this.content = content;
    }

    //Getters
    public int getNotificationId() { return notificationId; }
    public Date getCreatedOn() { return createdOn; }
    public String getContent() { return content; }

    //Setters
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }
    public void setCreatedOn(Date createdOn) { this.createdOn = createdOn; }
    public void setContent(String content) { this.content = content; }

    public abstract boolean sendNotification();
}