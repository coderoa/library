import java.util.ArrayList;
import java.util.List;

public class MemberAccount extends Account {
    private final List<BookItem> checkedOutItems = new ArrayList<>();
    private double outstandingFine;

    public MemberAccount(String id, String name, String email, String phone, Address address, LibraryCard libraryCard, AccountStatus status) {
        super(id, name, email, phone, address, libraryCard, status);
    }

    public List<BookItem> getCheckedOutItems() {
        return checkedOutItems;
    }

    public double getOutstandingFine() {
        return outstandingFine;
    }

    public void addFine(double amount) {
        outstandingFine += amount;
    }

    public void clearFines() {
        outstandingFine = 0;
    }
}
