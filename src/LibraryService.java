import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class LibraryService {
    public static final int MAX_CHECKOUTS = 5;
    public static final int MAX_LENDING_DAYS = 10;
    public static final double DAILY_FINE = 1.0;

    private final Library library;
    private final Catalog catalog;
    private final List<BookLending> lendings = new ArrayList<>();
    private final List<BookReservation> reservations = new ArrayList<>();
    private final List<Notification> notifications = new ArrayList<>();
    private final List<Fine> fines = new ArrayList<>();

    public LibraryService(Library library) {
        this.library = library;
        this.catalog = new Catalog(library.getInventory());
    }

    public Library getLibrary() {
        return library;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public List<BookLending> getLendings() {
        return lendings;
    }

    public List<BookReservation> getReservations() {
        return reservations;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public List<Fine> getFines() {
        return fines;
    }

    public List<BookItem> search(String mode, String query) {
        if (query == null || query.isBlank()) {
            return library.getInventory().stream()
                    .sorted(Comparator.comparing(item -> item.getBook().getTitle()))
                    .toList();
        }
        return switch (mode) {
            case "Author" -> catalog.searchByAuthor(query);
            case "Subject" -> catalog.searchBySubject(query);
            case "Publication Date" -> catalog.searchByPublicationDate(LocalDate.parse(query));
            default -> catalog.searchByTitle(query);
        };
    }

    public MemberAccount registerMember(String name, String email) {
        MemberAccount member = new MemberAccount(
                "M-" + (library.getMembers().size() + 1),
                name,
                email,
                "N/A",
                library.getAddress(),
                new LibraryCard("CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT), LocalDate.now(), AccountStatus.ACTIVE),
                AccountStatus.ACTIVE
        );
        library.getMembers().add(member);
        notifications.add(new Notification(member.getName(), "Membership created with card " + member.getLibraryCard().getBarcode(), LocalDateTime.now()));
        return member;
    }

    public void cancelMembership(MemberAccount member) {
        member.setStatus(AccountStatus.CANCELED);
        member.getLibraryCard().setStatus(AccountStatus.CANCELED);
        notifications.add(new Notification(member.getName(), "Membership canceled.", LocalDateTime.now()));
    }

    public void addBookItem(BookItem item) {
        library.getInventory().add(item);
    }

    public void removeBookItem(BookItem item) {
        library.getInventory().remove(item);
    }

    public String checkoutReservation(BookReservation reservation) {
        Optional<BookItem> copy = library.getInventory().stream()
                .filter(i -> i.getBook().getIsbn().equals(reservation.getBook().getIsbn()))
                .filter(i -> i.getStatus() == BookStatus.AVAILABLE || i.getStatus() == BookStatus.RESERVED)
                .findFirst();
        if (copy.isEmpty()) return "No available copy found for \"" + reservation.getBook().getTitle() + "\".";
        String result = checkoutBook(reservation.getMember(), copy.get());
        if (result.equals("Book checked out successfully.")) {
            reservation.setStatus(ReservationStatus.COMPLETED);
        }
        return result;
    }

    public String checkoutBook(MemberAccount member, BookItem item) {
        if (member.getStatus() != AccountStatus.ACTIVE) {
            return "Member account is not active.";
        }
        if (member.getCheckedOutItems().size() >= MAX_CHECKOUTS) {
            return "Member has reached the checkout limit of " + MAX_CHECKOUTS + ".";
        }
        if (item.getStatus() == BookStatus.LOANED) {
            return "Book item is already checked out.";
        }
        if (item.getStatus() == BookStatus.RESERVED) {
            Optional<BookReservation> reservation = nextReservation(item.getBook());
            if (reservation.isPresent() && reservation.get().getMember() != member) {
                return "Book item is reserved for another member.";
            }
            reservation.ifPresent(value -> value.setStatus(ReservationStatus.COMPLETED));
        }

        LocalDate dueDate = LocalDate.now().plusDays(MAX_LENDING_DAYS);
        item.setStatus(BookStatus.LOANED);
        item.setDueDate(dueDate);
        member.getCheckedOutItems().add(item);
        lendings.add(new BookLending("L-" + UUID.randomUUID().toString().substring(0, 8), item, member, LocalDate.now(), dueDate));
        notifications.add(new Notification(member.getName(), "Checked out " + item.getBook().getTitle() + ". Due on " + dueDate + ".", LocalDateTime.now()));
        return "Book checked out successfully.";
    }

    public String reserveBook(MemberAccount member, Book book) {
        boolean alreadyReserved = reservations.stream()
                .anyMatch(reservation -> reservation.getBook().getIsbn().equals(book.getIsbn())
                        && reservation.getMember() == member
                        && reservation.getStatus() != ReservationStatus.CANCELED
                        && reservation.getStatus() != ReservationStatus.COMPLETED);
        if (alreadyReserved) {
            return "Member already has an active reservation for this book.";
        }

        ReservationStatus status = hasAvailableCopy(book) ? ReservationStatus.PENDING_PICKUP : ReservationStatus.WAITING;
        reservations.add(new BookReservation("R-" + UUID.randomUUID().toString().substring(0, 8), book, member, LocalDate.now(), status));
        notifications.add(new Notification(member.getName(), "Reserved " + book.getTitle() + " (" + status + ").", LocalDateTime.now()));
        notifications.add(new Notification("LIBRARIAN", member.getName() + " reserved \"" + book.getTitle() + "\" — " + status, LocalDateTime.now()));
        return "Reservation created.";
    }

    public String renewBook(MemberAccount member, BookItem item) {
        Optional<BookLending> lending = activeLending(item);
        if (lending.isEmpty()) {
            return "No active lending found for this book item.";
        }
        if (lending.get().getMember() != member) {
            return "Book item is checked out by another member.";
        }
        Optional<BookReservation> nextReservation = nextReservation(item.getBook());
        if (nextReservation.isPresent() && nextReservation.get().getMember() != member) {
            return "Cannot renew. Another member has reserved this book.";
        }

        LocalDate newDueDate = LocalDate.now().plusDays(MAX_LENDING_DAYS);
        lending.get().setDueDate(newDueDate);
        item.setDueDate(newDueDate);
        notifications.add(new Notification(member.getName(), "Renewed " + item.getBook().getTitle() + ". New due date: " + newDueDate + ".", LocalDateTime.now()));
        return "Book renewed.";
    }

    public String returnBook(MemberAccount member, BookItem item) {
        Optional<BookLending> lending = activeLending(item);
        if (lending.isEmpty()) {
            return "No active lending found for this book item.";
        }
        if (lending.get().getMember() != member) {
            return "Book item belongs to another member.";
        }

        lending.get().setReturnDate(LocalDate.now());
        member.getCheckedOutItems().remove(item);
        long lateDays = Math.max(0, ChronoUnit.DAYS.between(lending.get().getDueDate(), LocalDate.now()));
        if (lateDays > 0) {
            double amount = lateDays * DAILY_FINE;
            member.addFine(amount);
            fines.add(new Fine(member.getName(), item.getBook().getTitle(), lateDays, amount));
            notifications.add(new Notification(member.getName(), "Returned late. Fine applied: $" + amount, LocalDateTime.now()));
        }

        Optional<BookReservation> nextReservation = nextReservation(item.getBook());
        if (nextReservation.isPresent()) {
            item.setStatus(BookStatus.RESERVED);
            item.setDueDate(null);
            nextReservation.get().setStatus(ReservationStatus.PENDING_PICKUP);
            notifications.add(new Notification(nextReservation.get().getMember().getName(),
                    "Reserved book now available: " + item.getBook().getTitle(), LocalDateTime.now()));
        } else {
            item.setStatus(BookStatus.AVAILABLE);
            item.setDueDate(null);
        }
        return "Book returned.";
    }

    public void sendOverdueNotifications() {
        lendings.stream()
                .filter(BookLending::isActive)
                .filter(lending -> lending.getDueDate().isBefore(LocalDate.now()))
                .forEach(lending -> notifications.add(new Notification(
                        lending.getMember().getName(),
                        "Overdue book: " + lending.getBookItem().getBook().getTitle() + ". Please return it immediately.",
                        LocalDateTime.now()
                )));
    }

    public List<BookItem> booksCheckedOutBy(MemberAccount member) {
        return List.copyOf(member.getCheckedOutItems());
    }

    public Optional<MemberAccount> whoBorrowed(BookItem item) {
        return activeLending(item).map(BookLending::getMember);
    }

    private Optional<BookLending> activeLending(BookItem item) {
        return lendings.stream()
                .filter(BookLending::isActive)
                .filter(lending -> lending.getBookItem().getBarcode().equals(item.getBarcode()))
                .findFirst();
    }

    private Optional<BookReservation> nextReservation(Book book) {
        return reservations.stream()
                .filter(reservation -> reservation.getBook().getIsbn().equals(book.getIsbn()))
                .filter(reservation -> reservation.getStatus() == ReservationStatus.WAITING || reservation.getStatus() == ReservationStatus.PENDING_PICKUP)
                .min(Comparator.comparing(BookReservation::getCreatedOn));
    }

    private boolean hasAvailableCopy(Book book) {
        return library.getInventory().stream()
                .anyMatch(item -> item.getBook().getIsbn().equals(book.getIsbn()) && item.getStatus() == BookStatus.AVAILABLE);
    }
}
