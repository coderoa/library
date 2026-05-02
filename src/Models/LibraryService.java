import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LibraryService {
    public static final int MAX_CHECKOUTS = 5;
    public static final int MAX_LENDING_DAYS = 10;
    public static final double DAILY_FINE = 1.0;

    private final Library library;
    private final Catalog catalog;
    private final List<BookLending> lendings;
    private final List<BookReservation> reservations;
    private final List<Notification> notifications;
    private final List<Fine> fines;

    private final BookItemDAO     bookItemDAO     = new BookItemDAO();
    private final MemberDAO       memberDAO       = new MemberDAO();
    private final LibrarianDAO    librarianDAO    = new LibrarianDAO();
    private final LendingDAO      lendingDAO      = new LendingDAO();
    private final ReservationDAO  reservationDAO  = new ReservationDAO();
    private final FineDAO         fineDAO         = new FineDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final LibraryCardDAO  libCardDAO      = new LibraryCardDAO();

    public LibraryService(Library library) {
        this.library = library;
        library.getInventory().addAll(bookItemDAO.getAll());
        library.getMembers().addAll(memberDAO.getAll());
        library.getLibrarians().addAll(librarianDAO.getAll());
        this.lendings      = new ArrayList<>(lendingDAO.getAll());
        this.reservations  = new ArrayList<>(reservationDAO.getAll());
        this.fines         = new ArrayList<>(fineDAO.getAll());
        this.notifications = new ArrayList<>(notificationDAO.getAll());
        this.catalog       = new Catalog(library.getInventory());
    }

    public Map<String, String> loadCredentials() {
        Map<String, String> creds = new HashMap<>();
        creds.putAll(memberDAO.getAllCredentials());
        creds.putAll(librarianDAO.getAllCredentials());
        return creds;
    }

    public Library getLibrary()                        { return library; }
    public Catalog getCatalog()                        { return catalog; }
    public List<BookLending> getLendings()             { return lendings; }
    public List<BookReservation> getReservations()     { return reservations; }
    public List<Notification> getNotifications()       { return notifications; }
    public List<Fine> getFines()                       { return fines; }

    public List<BookItem> search(String mode, String query) {
        if (query == null || query.isBlank()) {
            return library.getInventory().stream()
                    .sorted(Comparator.comparing(item -> item.getBook().getTitle()))
                    .toList();
        }
        return switch (mode) {
            case "Author"           -> catalog.searchByAuthor(query);
            case "Subject"          -> catalog.searchBySubject(query);
            case "Barcode"          -> catalog.searchByBarcode(query);
            case "Publication Date" -> catalog.searchByPublicationDate(LocalDate.parse(query));
            default                 -> catalog.searchByTitle(query);
        };
    }

    public LibrarianAccount registerLibrarian(String name, String email, String password) {
        String id = "L-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        LibraryCard card = new LibraryCard(
            "LIB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT),
            LocalDate.now(), AccountStatus.ACTIVE);
        LibrarianAccount librarian = new LibrarianAccount(
            id, name, email, "N/A", library.getAddress(), card, AccountStatus.ACTIVE);
        library.getLibrarians().add(librarian);
        librarianDAO.insert(librarian, password);
        return librarian;
    }

    public MemberAccount registerMember(String name, String email, String password) {
        String id = "M-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        LibraryCard card = new LibraryCard(
            "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT),
            LocalDate.now(), AccountStatus.ACTIVE);
        MemberAccount member = new MemberAccount(
            id, name, email, "N/A", library.getAddress(), card, AccountStatus.ACTIVE);
        library.getMembers().add(member);
        memberDAO.insert(member, password);
        addNotification(new Notification(member.getName(),
            "Membership created with card " + card.getBarcode(), LocalDateTime.now()));
        return member;
    }

    public String payFine(MemberAccount member) {
        if (member.getOutstandingFine() <= 0) return "No outstanding fine.";
        double amount = member.getOutstandingFine();
        member.clearFines();
        fineDAO.deleteByMember(member.getId());
        memberDAO.updateFine(member.getId(), 0.0);
        fines.removeIf(f -> f.memberId().equals(member.getId()));
        addNotification(new Notification(member.getName(),
            "Fine of $" + amount + " paid and cleared.", LocalDateTime.now()));
        return "Fine of $" + amount + " cleared.";
    }

    public void cancelMembership(MemberAccount member) {
        member.setStatus(AccountStatus.CANCELED);
        member.getLibraryCard().setStatus(AccountStatus.CANCELED);
        memberDAO.updateStatus(member.getId(), AccountStatus.CANCELED);
        libCardDAO.updateStatus(member.getLibraryCard().getBarcode(), AccountStatus.CANCELED);
        addNotification(new Notification(member.getName(), "Membership canceled.", LocalDateTime.now()));
    }

    public void addBookItem(BookItem item) {
        library.getInventory().add(item);
        bookItemDAO.insert(item);
    }

    public void removeBookItem(BookItem item) {
        library.getInventory().remove(item);
        bookItemDAO.delete(item.getBarcode());
    }

    public String checkoutReservation(BookReservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING_PICKUP)
            return "This reservation is no longer pending pickup.";
        Optional<BookItem> copy = library.getInventory().stream()
                .filter(i -> i.getBook().getIsbn().equals(reservation.getBook().getIsbn()))
                .filter(i -> i.getStatus() == BookStatus.AVAILABLE || i.getStatus() == BookStatus.RESERVED)
                .findFirst();
        if (copy.isEmpty()) return "No available copy found for \"" + reservation.getBook().getTitle() + "\".";
        String result = checkoutBook(reservation.getMember(), copy.get());
        if (result.equals("Book checked out successfully.")) {
            reservation.setStatus(ReservationStatus.COMPLETED);
            reservationDAO.updateStatus(reservation.getId(), ReservationStatus.COMPLETED);
        }
        return result;
    }

    public String checkoutBook(MemberAccount member, BookItem item) {
        if (member.getStatus() != AccountStatus.ACTIVE)
            return "Member account is not active.";
        if (item.isReferenceOnly())
            return "\"" + item.getBook().getTitle() + "\" is reference-only and cannot be checked out.";
        if (member.getCheckedOutItems().size() >= MAX_CHECKOUTS)
            return "Member has reached the checkout limit of " + MAX_CHECKOUTS + ".";
        if (item.getStatus() == BookStatus.LOANED)
            return "Book item is already checked out.";
        if (item.getStatus() == BookStatus.RESERVED) {
            Optional<BookReservation> reservation = nextReservation(item.getBook());
            if (reservation.isPresent() && reservation.get().getMember() != member)
                return "Book item is reserved for another member.";
            reservation.ifPresent(r -> {
                r.setStatus(ReservationStatus.COMPLETED);
                reservationDAO.updateStatus(r.getId(), ReservationStatus.COMPLETED);
            });
        }

        LocalDate dueDate = LocalDate.now().plusDays(MAX_LENDING_DAYS);
        item.setStatus(BookStatus.LOANED);
        item.setDueDate(dueDate);
        member.getCheckedOutItems().add(item);
        bookItemDAO.updateStatus(item);

        BookLending lending = new BookLending(
            "L-" + UUID.randomUUID().toString().substring(0, 8), item, member, LocalDate.now(), dueDate);
        lendings.add(lending);
        lendingDAO.insert(lending);

        addNotification(new Notification(member.getName(),
            "Checked out " + item.getBook().getTitle() + ". Due on " + dueDate + ".", LocalDateTime.now()));
        return "Book checked out successfully.";
    }

    public String reserveBook(MemberAccount member, Book book) {
        return doReserveBook(member, book, null);
    }

    public String reserveBook(MemberAccount member, BookItem specificItem) {
        return doReserveBook(member, specificItem.getBook(), specificItem);
    }

    private String doReserveBook(MemberAccount member, Book book, BookItem preferredItem) {
        boolean alreadyReserved = reservations.stream()
                .anyMatch(r -> r.getBook().getIsbn().equals(book.getIsbn())
                        && r.getMember() == member
                        && r.getStatus() != ReservationStatus.CANCELED
                        && r.getStatus() != ReservationStatus.COMPLETED);
        if (alreadyReserved)
            return "Member already has an active reservation for this book.";

        ReservationStatus status = hasAvailableCopy(book)
            ? ReservationStatus.PENDING_PICKUP : ReservationStatus.WAITING;
        BookReservation reservation = new BookReservation(
            "R-" + UUID.randomUUID().toString().substring(0, 8), book, member, LocalDate.now(), status);
        reservations.add(reservation);
        reservationDAO.insert(reservation);

        if (status == ReservationStatus.PENDING_PICKUP) {
            BookItem toReserve = (preferredItem != null && preferredItem.getStatus() == BookStatus.AVAILABLE)
                    ? preferredItem
                    : library.getInventory().stream()
                            .filter(i -> i.getBook().getIsbn().equals(book.getIsbn()))
                            .filter(i -> i.getStatus() == BookStatus.AVAILABLE)
                            .findFirst().orElse(null);
            if (toReserve != null) {
                toReserve.setStatus(BookStatus.RESERVED);
                bookItemDAO.updateStatus(toReserve);
            }
        }

        addNotification(new Notification(member.getName(),
            "Reserved " + book.getTitle() + " (" + status + ").", LocalDateTime.now()));
        addNotification(new Notification("LIBRARIAN",
            member.getName() + " reserved \"" + book.getTitle() + "\" — " + status, LocalDateTime.now()));
        return "Reservation created.";
    }

    public String renewBook(MemberAccount member, BookItem item) {
        Optional<BookLending> lending = activeLending(item);
        if (lending.isEmpty())
            return "No active lending found for this book item.";
        if (lending.get().getMember() != member)
            return "Book item is checked out by another member.";
        Optional<BookReservation> nextRes = nextReservation(item.getBook());
        if (nextRes.isPresent() && nextRes.get().getMember() != member)
            return "Cannot renew. Another member has reserved this book.";

        LocalDate newDueDate = LocalDate.now().plusDays(MAX_LENDING_DAYS);
        lending.get().setDueDate(newDueDate);
        item.setDueDate(newDueDate);
        lendingDAO.updateDueDate(lending.get());
        bookItemDAO.updateStatus(item);

        addNotification(new Notification(member.getName(),
            "Renewed " + item.getBook().getTitle() + ". New due date: " + newDueDate + ".", LocalDateTime.now()));
        return "Book renewed.";
    }

    public String returnBook(MemberAccount member, BookItem item) {
        Optional<BookLending> lending = activeLending(item);
        if (lending.isEmpty())
            return "No active lending found for this book item.";
        if (lending.get().getMember() != member)
            return "Book item belongs to another member.";

        lending.get().setReturnDate(LocalDate.now());
        member.getCheckedOutItems().remove(item);
        lendingDAO.updateReturn(lending.get());

        long lateDays = Math.max(0, ChronoUnit.DAYS.between(lending.get().getDueDate(), LocalDate.now()));
        if (lateDays > 0) {
            double amount = lateDays * DAILY_FINE;
            member.addFine(amount);
            Fine fine = new Fine(member.getId(), member.getName(), item.getBook().getTitle(), lateDays, amount);
            fines.add(fine);
            fineDAO.insert(fine);
            memberDAO.updateFine(member.getId(), member.getOutstandingFine());
            addNotification(new Notification(member.getName(),
                "Returned late. Fine applied: $" + amount, LocalDateTime.now()));
        }

        Optional<BookReservation> next = nextReservation(item.getBook());
        if (next.isPresent()) {
            item.setStatus(BookStatus.RESERVED);
            item.setDueDate(null);
            next.get().setStatus(ReservationStatus.PENDING_PICKUP);
            reservationDAO.updateStatus(next.get().getId(), ReservationStatus.PENDING_PICKUP);
            addNotification(new Notification(next.get().getMember().getName(),
                "Reserved book now available: " + item.getBook().getTitle(), LocalDateTime.now()));
        } else {
            item.setStatus(BookStatus.AVAILABLE);
            item.setDueDate(null);
        }
        bookItemDAO.updateStatus(item);
        return "Book returned.";
    }

    public void accumulateOverdueFines() {
        lendings.stream()
                .filter(BookLending::isActive)
                .filter(l -> l.getDueDate().isBefore(LocalDate.now()))
                .forEach(l -> l.getMember().addFine(DAILY_FINE));
    }

    public void persistMemberFines() {
        library.getMembers().forEach(m -> memberDAO.updateFine(m.getId(), m.getOutstandingFine()));
    }

    public void sendOverdueNotifications() {
        lendings.stream()
                .filter(BookLending::isActive)
                .filter(l -> l.getDueDate().isBefore(LocalDate.now()))
                .forEach(l -> addNotification(new Notification(
                        l.getMember().getName(),
                        "Overdue book: " + l.getBookItem().getBook().getTitle() + ". Please return it immediately.",
                        LocalDateTime.now())));
    }

    public List<BookItem> booksCheckedOutBy(MemberAccount member) {
        return List.copyOf(member.getCheckedOutItems());
    }

    public Optional<MemberAccount> whoBorrowed(BookItem item) {
        return activeLending(item).map(BookLending::getMember);
    }

    private void addNotification(Notification n) {
        notifications.add(n);
        notificationDAO.insert(n);
    }

    private Optional<BookLending> activeLending(BookItem item) {
        return lendings.stream()
                .filter(BookLending::isActive)
                .filter(l -> l.getBookItem().getBarcode().equals(item.getBarcode()))
                .findFirst();
    }

    private Optional<BookReservation> nextReservation(Book book) {
        return reservations.stream()
                .filter(r -> r.getBook().getIsbn().equals(book.getIsbn()))
                .filter(r -> r.getStatus() == ReservationStatus.WAITING
                          || r.getStatus() == ReservationStatus.PENDING_PICKUP)
                .min(Comparator.comparing(BookReservation::getCreatedOn));
    }

    private boolean hasAvailableCopy(Book book) {
        return library.getInventory().stream()
                .anyMatch(i -> i.getBook().getIsbn().equals(book.getIsbn())
                            && i.getStatus() == BookStatus.AVAILABLE);
    }
}