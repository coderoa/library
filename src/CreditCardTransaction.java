import java.util.Date;

public class CreditCardTransaction extends FineTransaction {
    private String nameOnCard;

    public CreditCardTransaction(Date creationDate, double amount, String nameOnCard) {
        super(creationDate, amount);
        this.nameOnCard = nameOnCard;
    }

    public String getNameOnCard() { return nameOnCard; }
    public void setNameOnCard(String nameOnCard) { this.nameOnCard = nameOnCard; }

    @Override
    public boolean initiateTransaction() {
        System.out.println("Processing credit card payment for: " + nameOnCard);
        System.out.println("Amount: " + getAmount());
        return true;
    }
}