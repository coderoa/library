package Exceptions;

public class InvalidBarcodeException extends Exception {
    public InvalidBarcodeException(String message) {
        super(message);
    }
}
