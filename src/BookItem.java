import java.time.LocalDate;

public class BookItem {
    private final String barcode;
    private final Book book;
    private final Rack rack;
    private BookStatus status;
    private LocalDate dueDate;

    public BookItem(String barcode, Book book, Rack rack, BookStatus status) {
        this.barcode = barcode;
        this.book = book;
        this.rack = rack;
        this.status = status;
    }

    public String getBarcode() {
        return barcode;
    }

    public Book getBook() {
        return book;
    }

    public Rack getRack() {
        return rack;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
