import enums.ReservationStatus;
import java.util.Date;

public class BookReservation {
    private Date creationDate;
    private ReservationStatus status;
    private String bookItemBarcode;
    private String memberId;

    //Constructor
    public BookReservation(Date creationDate, ReservationStatus status,  String bookItemBarcode, String memberId) {
        this.creationDate = creationDate;
        this.status = status;
        this.bookItemBarcode = bookItemBarcode;
        this.memberId = memberId;
    }

    //Getters
    public Date getCreationDate() { return creationDate; }
    public ReservationStatus getStatus() { return status; }
    public String getBookItemBarcode() { return bookItemBarcode; }
    public String getMemberId() { return memberId; }

    //Setters
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public void setBookItemBarcode(String bookItemBarcode) { this.bookItemBarcode = bookItemBarcode; }
    public void setMemberId(String memberId) { this.memberId = memberId; }


    public BookReservation fetchReservationDetails(String barcode) {
        // logic will be added later
        return null;
    }
}