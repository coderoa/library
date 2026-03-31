import java.util.Date;

public abstract class FineTransaction {
    private Date creationDate;
    private double amount;

    public FineTransaction(Date creationDate, double amount) {
        this.creationDate = creationDate;
        this.amount = amount;
    }

    public Date getCreationDate() { return creationDate; }
    public double getAmount() { return amount; }

    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }
    public void setAmount(double amount) { this.amount = amount; }

    public abstract boolean initiateTransaction();
}