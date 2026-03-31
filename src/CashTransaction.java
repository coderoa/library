import java.util.Date;

public class CashTransaction extends FineTransaction {
    private double cashTendered;

    //Constructor
    public CashTransaction(Date creationDate, double amount, double cashTendered) {
        super(creationDate, amount);
        this.cashTendered = cashTendered;
    }

    //Getters
    public double getCashTendered() { return cashTendered; }
    public void setCashTendered(double cashTendered) { this.cashTendered = cashTendered; }

    @Override
    public boolean initiateTransaction() {
        System.out.println("Processing cash payment");
        System.out.println("Amount: " + getAmount());
        System.out.println("Cash tendered: " + cashTendered);
        System.out.println("Change: " + (cashTendered - getAmount()));
        return true;
    }
}