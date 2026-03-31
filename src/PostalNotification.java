public class PostalNotification extends Notification {
    private Address address;

    //Constructor
    public PostalNotification(int notificationId, java.util.Date createdOn, String content, Address address) {
        super(notificationId, createdOn, content);
        this.address = address;
    }

    //Getters
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    @Override
    public boolean sendNotification() {
        // postal sending logic will be added later
        System.out.println("Sending postal to: " + address.getCity());
        System.out.println("Content: " + getContent());
        return true;
    }
}