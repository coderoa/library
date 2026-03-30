public class Rack {
    private int number;
    private String locationIdentifier;

    //Constructor
    public Rack(int number, String locationIdentifier) {
        this.number = number;
        this.locationIdentifier = locationIdentifier;
    }

    //Getters
    public int getNumber() { return number; }
    public String getLocationIdentifier() { return locationIdentifier; }

    //Setters
    public void setNumber(int number) { this.number = number; }
    public void setLocationIdentifier(String locationIdentifier) { this.locationIdentifier = locationIdentifier; }
}