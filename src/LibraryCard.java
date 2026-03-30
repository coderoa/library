import java.util.Date;

public class LibraryCard {
    private String cardNumber;
    private String barcode;
    private Date issuedAt;
    private boolean active;

    //Constructor
    public LibraryCard(String cardNumber, String barcode, Date issuedAt, boolean active) {
        this.cardNumber = cardNumber;
        this.barcode = barcode;
        this.issuedAt = issuedAt;
        this.active = active;
    }

    //Getters
    public String getCardNumber() { return cardNumber; }
    public String getBarcode() { return barcode; }
    public Date getIssuedAt() { return issuedAt; }
    public boolean isActive() { return active; }

    //Setters
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setIssuedAt(Date issuedAt) { this.issuedAt = issuedAt; }
    public void setActive(boolean active) { this.active = active; }


}