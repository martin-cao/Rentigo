package cc.martincao.rentigo.rentigobackend.rental.model;

public enum RentalStatus {
    PENDING_PAYMENT,           // 0 - 等待支付定金
    PENDING_RENTAL_PAYMENT,    // 1 - 定金已支付，等待支付租金
    PAID,                     // 2 - 租金已支付
    ACTIVE,                   // 3 - 进行中（已提车）
    PENDING_OVERTIME_PAYMENT,  // 4 - 等待支付超时费用
    FINISHED,                 // 5 - 已完成
    CANCELLED                 // 6 - 已取消
}
