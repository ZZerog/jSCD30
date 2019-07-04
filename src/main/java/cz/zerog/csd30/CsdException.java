package cz.zerog.csd30;


public class CsdException extends RuntimeException {

    public CsdException(Exception e) {
        super(e);
    }

    public CsdException(String message) {
        super(message);
    }
}
