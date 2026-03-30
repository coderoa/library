import java.util.Date;

public class Fine {
    private Date creationDate;
    private double amount;
    private String bookItemBarcode;
    private String memberId;

    //Constructor
    public Fine(Date creationDate, double amount,  String bookItemBarcode, String memberId) {
        this.creationDate = creationDate;
        this.amount = amount;
        this.bookItemBarcode = bookItemBarcode;
        this.memberId = memberId;
    }

    //Getters
    public Date getCreationDate() { return creationDate; }
    public double getAmount() { return amount; }
    public String getBookItemBarcode() { return bookItemBarcode; }
    public String getMemberId() { return memberId; }

    //Setters
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setBookItemBarcode(String barcode) { this.bookItemBarcode = barcode; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    //Method for calculating Fine
    public static double calculateFine(int daysLate) {
        double finePerDay = 1.0;
        return daysLate * finePerDay;
    }
}