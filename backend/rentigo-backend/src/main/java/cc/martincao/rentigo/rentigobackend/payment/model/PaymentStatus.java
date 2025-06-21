package cc.martincao.rentigo.rentigobackend.payment.model;

/**
 * 支付状态枚举
 */
public enum PaymentStatus {
    /**
     * 待支付
     */
    PENDING(0, "待支付"),
    
    /**
     * 支付成功
     */
    SUCCESS(1, "支付成功"),
    
    /**
     * 支付失败
     */
    FAILED(2, "支付失败"),
    
    /**
     * 已退款
     */
    REFUNDED(3, "已退款");

    private final int code;
    private final String description;

    PaymentStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentStatus fromCode(int code) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status code: " + code);
    }
}
