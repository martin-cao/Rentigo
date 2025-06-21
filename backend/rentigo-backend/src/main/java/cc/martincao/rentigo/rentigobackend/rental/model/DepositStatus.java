package cc.martincao.rentigo.rentigobackend.rental.model;

/**
 * 押金状态枚举
 */
public enum DepositStatus {
    /**
     * 未收取押金
     */
    NOT_COLLECTED(0, "未收取"),
    
    /**
     * 已收取押金
     */
    COLLECTED(1, "已收取"),
    
    /**
     * 已退还押金
     */
    RETURNED(2, "已退还"),
    
    /**
     * 已没收押金
     */
    CONFISCATED(3, "已没收"),
    
    /**
     * 部分没收押金
     */
    PARTIALLY_CONFISCATED(4, "部分没收");

    private final int code;
    private final String description;

    DepositStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DepositStatus fromCode(int code) {
        for (DepositStatus status : DepositStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown deposit status code: " + code);
    }
}
