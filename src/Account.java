public abstract class Account {
    private final String id;
    private final String name;
    private final String email;
    private final String phone;
    private final Address address;
    private final LibraryCard libraryCard;
    private AccountStatus status;

    protected Account(String id, String name, String email, String phone, Address address, LibraryCard libraryCard, AccountStatus status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.libraryCard = libraryCard;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Address getAddress() {
        return address;
    }

    public LibraryCard getLibraryCard() {
        return libraryCard;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name + " [" + libraryCard.getBarcode() + "]";
    }
}
