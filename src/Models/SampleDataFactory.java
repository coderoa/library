import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class SampleDataFactory {
    private SampleDataFactory() {}

    public static void seed(LibraryService service, Map<String, String> credentials) {
        // Librarian
        LibrarianAccount librarian = service.registerLibrarian(
            "Dilshod Karimov", "librarian@library.local", "admin123");
        credentials.put(librarian.getEmail(), "admin123");

        // Members
        MemberAccount alice = service.registerMember("Alice Johnson", "alice@example.com", "alice123");
        credentials.put(alice.getEmail(), "alice123");

        MemberAccount bob = service.registerMember("Bob Smith", "bob@example.com", "bob123");
        credentials.put(bob.getEmail(), "bob123");

        // Books
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

        service.addBookItem(new BookItem("BOOK-1001", cleanCode,       new Rack("R1", "A-01"), BookStatus.AVAILABLE));
        service.addBookItem(new BookItem("BOOK-1002", cleanCode,       new Rack("R1", "A-01"), BookStatus.AVAILABLE));
        service.addBookItem(new BookItem("BOOK-1003", effectiveJava,   new Rack("R2", "A-02"), BookStatus.AVAILABLE));
        service.addBookItem(new BookItem("BOOK-1004", designPatterns,  new Rack("R3", "B-01"), BookStatus.AVAILABLE));

        // Initial transactions — these also persist via service
        service.checkoutBook(alice, service.getLibrary().getInventory().get(2));
        service.reserveBook(bob, effectiveJava);
    }
}
