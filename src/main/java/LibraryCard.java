import java.time.LocalDate;

public class LibraryCard {
    private final String barcode;
    private final LocalDate issuedOn;
    private AccountStatus status;

    public LibraryCard(String barcode, LocalDate issuedOn, AccountStatus status) {
        this.barcode = barcode;
        this.issuedOn = issuedOn;
        this.status = status;
    }

    public String getBarcode() {
        return barcode;
    }

    public LocalDate getIssuedOn() {
        return issuedOn;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
