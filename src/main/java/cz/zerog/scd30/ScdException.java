package cz.zerog.scd30;


public class ScdException extends RuntimeException {

    public ScdException(Exception e) {
        super(e);
    }

    public ScdException(String message) {
        super(message);
    }
}
