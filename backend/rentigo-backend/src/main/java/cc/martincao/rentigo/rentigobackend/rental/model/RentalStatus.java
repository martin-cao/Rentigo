package cc.martincao.rentigo.rentigobackend.rental.model;

public enum RentalStatus {
    PENDING_PAYMENT,  // 0 - 待支付
    PAID,            // 1 - 已支付
    ACTIVE,          // 2 - 进行中（已提车）
    FINISHED,        // 3 - 已完成
    CANCELLED        // 4 - 已取消
}
