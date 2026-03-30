import enums.AccountStatus;

public class Librarian extends Account {

    //constructor
    public Librarian(String username, String password, AccountStatus status,
                     String name, String email, String phone, Address address) {
        super(username, password, status, name, email, phone, address);
    }


    public boolean addBookItem(BookItem bookItem) {
        // logic will be added later
        return true;
    }

    //Method for blocking
    public boolean blockMember(Member member) {
        member.setStatus(AccountStatus.BLACKLISTED);
        return true;
    }

    //Method for unblocking
    public boolean unblockMember(Member member) {
        member.setStatus(AccountStatus.ACTIVE);
        return true;
    }

    @Override
    public boolean resetPassword(String newPassword) {
        this.setPassword(newPassword);
        return true;
    }
}