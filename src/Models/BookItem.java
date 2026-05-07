import java.time.LocalDate;

public class BookItem {
    private final String barcode;
    private final Book book;
    private final Rack rack;
    private BookStatus status;
    private LocalDate dueDate;
    private final BookFormat format;
    private final boolean referenceOnly;

    public BookItem(String barcode, Book book, Rack rack, BookStatus status,
                    BookFormat format, boolean referenceOnly) {
        this.barcode = barcode;
        this.book = book;
        this.rack = rack;
        this.status = status;
        this.format = format;
        this.referenceOnly = referenceOnly;
    }

    public BookItem(String barcode, Book book, Rack rack, BookStatus status) {
        this(barcode, book, rack, status, BookFormat.HARDCOVER, false);
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

    public BookFormat getFormat() {
        return format;
    }

    public boolean isReferenceOnly() {
        return referenceOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookItem other)) return false;
        // Two BookItem instances are considered equal if they refer to the same
        // copy of a book, identified uniquely by its barcode.
        return java.util.Objects.equals(this.barcode, other.barcode);
    }

    @Override
    public int hashCode() {
        // Compute hash based solely on the barcode so BookItem instances can be
        // reliably used in hashed collections and comparisons. The barcode
        // uniquely identifies a physical copy of a book in the system.
        return java.util.Objects.hash(this.barcode);
    }
}
