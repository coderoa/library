import java.time.LocalDate;
import java.util.List;

public final class SampleDataFactory {
    private SampleDataFactory() {
    }

    public static LibraryService create() {
        Address address = new Address("12 Knowledge Ave", "Tashkent", "Tashkent", "100000", "Uzbekistan");
        Library library = new Library("Code Switchers Library", address);
        LibraryService service = new LibraryService(library);

        LibrarianAccount librarian = new LibrarianAccount(
                "L-1",
                "Dilshod Karimov",
                "librarian@library.local",
                "+998901112233",
                address,
                new LibraryCard("LIB-1001", LocalDate.now().minusMonths(6), AccountStatus.ACTIVE),
                AccountStatus.ACTIVE
        );
        library.getLibrarians().add(librarian);

        MemberAccount alice = new MemberAccount(
                "M-1",
                "Alice Johnson",
                "alice@example.com",
                "+998900000001",
                address,
                new LibraryCard("CARD-2001", LocalDate.now().minusMonths(3), AccountStatus.ACTIVE),
                AccountStatus.ACTIVE
        );
        MemberAccount bob = new MemberAccount(
                "M-2",
                "Bob Smith",
                "bob@example.com",
                "+998900000002",
                address,
                new LibraryCard("CARD-2002", LocalDate.now().minusMonths(2), AccountStatus.ACTIVE),
                AccountStatus.ACTIVE
        );
        library.getMembers().addAll(List.of(alice, bob));

        Book cleanCode = new Book("9780132350884", "Clean Code", "Software Engineering", "Prentice Hall",
                LocalDate.of(2008, 8, 1), List.of(new Author("A-1", "Robert C. Martin")));
        Book effectiveJava = new Book("9780134685991", "Effective Java", "Java", "Addison-Wesley",
                LocalDate.of(2018, 1, 6), List.of(new Author("A-2", "Joshua Bloch")));
        Book designPatterns = new Book("9780201633610", "Design Patterns", "Software Architecture", "Addison-Wesley",
                LocalDate.of(1994, 10, 21), List.of(
                new Author("A-3", "Erich Gamma"),
                new Author("A-4", "Richard Helm"),
                new Author("A-5", "Ralph Johnson"),
                new Author("A-6", "John Vlissides")));

        service.addBookItem(new BookItem("BOOK-1001", cleanCode, new Rack("R1", "A-01"), BookStatus.AVAILABLE));
        service.addBookItem(new BookItem("BOOK-1002", cleanCode, new Rack("R1", "A-01"), BookStatus.AVAILABLE));
        service.addBookItem(new BookItem("BOOK-1003", effectiveJava, new Rack("R2", "A-02"), BookStatus.AVAILABLE));
        service.addBookItem(new BookItem("BOOK-1004", designPatterns, new Rack("R3", "B-01"), BookStatus.AVAILABLE));

        service.checkoutBook(alice, library.getInventory().get(2));
        service.reserveBook(bob, effectiveJava);
        service.sendOverdueNotifications();
        return service;
    }
}
