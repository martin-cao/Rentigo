package cc.martincao.rentigo.rentigobackend.rental.exception;

public class RentalBusinessException extends RuntimeException {
    public RentalBusinessException(String message) {
        super(message);
    }
}
