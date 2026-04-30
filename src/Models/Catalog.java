import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Catalog {
    private final Collection<BookItem> inventory;

    public Catalog(Collection<BookItem> inventory) {
        this.inventory = inventory;
    }

    public List<BookItem> searchByTitle(String query) {
        return inventory.stream().filter(item -> contains(item.getBook().getTitle(), query)).toList();
    }

    public List<BookItem> searchByAuthor(String query) {
        return inventory.stream().filter(item -> contains(item.getBook().getAuthorNames(), query)).toList();
    }

    public List<BookItem> searchBySubject(String query) {
        return inventory.stream().filter(item -> contains(item.getBook().getSubject(), query)).toList();
    }

    public List<BookItem> searchByBarcode(String query) {
        return inventory.stream().filter(item -> contains(item.getBarcode(), query)).toList();
    }

    public List<BookItem> searchByPublicationDate(LocalDate date) {
        return inventory.stream().filter(item -> Objects.equals(item.getBook().getPublicationDate(), date)).toList();
    }

    private boolean contains(String value, String query) {
        return value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }
}
