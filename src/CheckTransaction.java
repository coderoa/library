import java.util.Date;

public class CheckTransaction extends FineTransaction {
    private String bankName;
    private String checkNumber;

    public CheckTransaction(Date creationDate, double amount,
                            String bankName, String checkNumber) {
        super(creationDate, amount);
        this.bankName = bankName;
        this.checkNumber = checkNumber;
    }

    public String getBankName() { return bankName; }
    public String getCheckNumber() { return checkNumber; }

    public void setBankName(String bankName) { this.bankName = bankName; }
    public void setCheckNumber(String checkNumber) { this.checkNumber = checkNumber; }

    @Override
    public boolean initiateTransaction() {
        System.out.println("Processing check payment from bank: " + bankName);
        System.out.println("Check number: " + checkNumber);
        return true;
    }
}