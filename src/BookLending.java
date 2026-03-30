import java.util.Date;

public class BookLending {
    private Date creationDate;
    private Date dueDate;
    private Date returnDate;
    private String bookItemBarcode;
    private String memberId;

    //Constructor
    public BookLending(Date creationDate, Date dueDate, String bookItemBarcode, String memberId) {
        this.creationDate = creationDate;
        this.dueDate = dueDate;
        this.bookItemBarcode = bookItemBarcode;
        this.memberId = memberId;
    }

    //Getters
    public Date getCreationDate() { return creationDate; }
    public Date getDueDate() { return dueDate; }
    public Date getReturnDate() { return returnDate; }
    public String getBookItemBarcode() { return bookItemBarcode; }
    public String getMemberId() { return memberId; }

    //Setters
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
    public void setBookItemBarcode(String barcode) { this.bookItemBarcode = barcode; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
}