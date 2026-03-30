import enums.AccountStatus;


public abstract class Account extends Person {
    private String username;
    private String password;
    private AccountStatus status;


    public Account(String username, String password, AccountStatus status,
                   String name, String email, String phone, Address address) {
        super(name, email, phone, address);
        this.username = username;
        this.password = password;
        this.status = status;
    }


    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public AccountStatus getStatus() { return status; }


    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setStatus(AccountStatus status) { this.status = status; }


    public abstract boolean resetPassword(String newPassword);
}