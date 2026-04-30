import java.util.ArrayList;
import java.util.List;

public class Library {
    private final String name;
    private final Address address;
    private final List<BookItem> inventory = new ArrayList<>();
    private final List<MemberAccount> members = new ArrayList<>();
    private final List<LibrarianAccount> librarians = new ArrayList<>();

    public Library(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public List<BookItem> getInventory() {
        return inventory;
    }

    public List<MemberAccount> getMembers() {
        return members;
    }

    public List<LibrarianAccount> getLibrarians() {
        return librarians;
    }
}
