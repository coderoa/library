import enums.AccountStatus;
import java.util.Date;

public class Member extends Account {
    private Date dateOfMembership;
    private int totalBooksCheckedout;

    //Constructor
    public Member(String username, String password, AccountStatus status,
                  String name, String email, String phone, Address address, Date dateOfMembership) {
        super(username, password, status, name, email, phone, address);
        this.dateOfMembership = dateOfMembership;
        this.totalBooksCheckedout = 0;
    }

    //Getters
    public Date getDateOfMembership() { return dateOfMembership; }
    public int getTotalBooksCheckedout() { return totalBooksCheckedout; }

    //Setters
    public void setDateOfMembership(Date dateOfMembership) { this.dateOfMembership = dateOfMembership; }
    public void setTotalBooksCheckedout(int totalBooksCheckedout) { this.totalBooksCheckedout = totalBooksCheckedout; }


    @Override
    public boolean resetPassword(String newPassword) {
        this.setPassword(newPassword);
        return true;
    }
}