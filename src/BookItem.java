import enums.BookFormat;
import enums.BookStatus;

import java.util.Date;
import java.util.List;

public class BookItem extends Book {
    private String barcode;
    private boolean isReferenceOnly;
    private Date borrowed;
    private Date dueDate;
    private double price;
    private BookFormat format;
    private BookStatus status;
    private Date dateOfPurchase;
    private Rack placedAt;

    //Constructor
    public BookItem(String barcode, boolean isReferenceOnly, double price,
                    BookFormat format, BookStatus status, Date dateOfPurchase,
                    Rack placedAt, String ISBN, String title, String subject,
                    String publisher, String language, int numberOfPages,
                    Date publicationDate, List<Author> authors) {
        super(ISBN, title, subject, publisher, language, numberOfPages, publicationDate, authors);
        this.barcode = barcode;
        this.isReferenceOnly = isReferenceOnly;
        this.price = price;
        this.format = format;
        this.status = status;
        this.dateOfPurchase = dateOfPurchase;
        this.placedAt = placedAt;
    }

    //Getters
    public String getBarcode() { return barcode; }
    public boolean isReferenceOnly() { return isReferenceOnly; }
    public Date getBorrowed() { return borrowed; }
    public Date getDueDate() { return dueDate; }
    public double getPrice() { return price; }
    public BookFormat getFormat() { return format; }
    public BookStatus getStatus() { return status; }
    public Date getDateOfPurchase() { return dateOfPurchase; }
    public Rack getPlacedAt() { return placedAt; }

    //Setters
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setReferenceOnly(boolean referenceOnly) { isReferenceOnly = referenceOnly; }
    public void setBorrowed(Date borrowed) { this.borrowed = borrowed; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public void setPrice(double price) { this.price = price; }
    public void setFormat(BookFormat format) { this.format = format; }
    public void setStatus(BookStatus status) { this.status = status; }
    public void setDateOfPurchase(Date dateOfPurchase) { this.dateOfPurchase = dateOfPurchase; }
    public void setPlacedAt(Rack placedAt) { this.placedAt = placedAt; }

    public boolean checkout() {
        if (this.isReferenceOnly) {
            return false;
        }
        this.status = BookStatus.LOANED;
        return true;
    }
}