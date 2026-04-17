import java.time.LocalDate;

public class BookLending {
    private final String id;
    private final BookItem bookItem;
    private final MemberAccount member;
    private final LocalDate checkoutDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    public BookLending(String id, BookItem bookItem, MemberAccount member, LocalDate checkoutDate, LocalDate dueDate) {
        this.id = id;
        this.bookItem = bookItem;
        this.member = member;
        this.checkoutDate = checkoutDate;
        this.dueDate = dueDate;
    }

    public String getId() {
        return id;
    }

    public BookItem getBookItem() {
        return bookItem;
    }

    public MemberAccount getMember() {
        return member;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isActive() {
        return returnDate == null;
    }
}
