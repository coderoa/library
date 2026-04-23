package Exceptions;

public class FineNotPaidException extends RuntimeException {
    public FineNotPaidException(String message) {
        super(message);
    }
}
