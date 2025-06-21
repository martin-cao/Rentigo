package cc.martincao.rentigo.rentigobackend.payment.model;

/**
 * 支付类型枚举
 */
public enum PaymentType {
    /**
     * 租金支付
     */
    RENTAL(0, "租金"),
    
    /**
     * 押金支付
     */
    DEPOSIT(1, "押金"),
    
    /**
     * 超时费用
     */
    OVERTIME(2, "超时费用");

    private final int code;
    private final String description;

    PaymentType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentType fromCode(int code) {
        for (PaymentType type : PaymentType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown payment type code: " + code);
    }
}
