package Exceptions;

public class InvalidFineAmountException extends RuntimeException {
    public InvalidFineAmountException(String message) {
        super(message);
    }
}
