import java.time.LocalDate;

public class BookReservation {
    private final String id;
    private final Book book;
    private final MemberAccount member;
    private final LocalDate createdOn;
    private ReservationStatus status;

    public BookReservation(String id, Book book, MemberAccount member, LocalDate createdOn, ReservationStatus status) {
        this.id = id;
        this.book = book;
        this.member = member;
        this.createdOn = createdOn;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public MemberAccount getMember() {
        return member;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
