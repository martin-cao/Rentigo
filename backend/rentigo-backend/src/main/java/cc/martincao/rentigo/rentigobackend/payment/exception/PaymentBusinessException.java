package cc.martincao.rentigo.rentigobackend.payment.exception;

/**
 * 支付业务异常
 */
public class PaymentBusinessException extends RuntimeException {
    
    public PaymentBusinessException(String message) {
        super(message);
    }
    
    public PaymentBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
