package cc.martincao.rentigo.rentigobackend.payment.dto;

import cc.martincao.rentigo.rentigobackend.payment.model.PaymentStatus;
import cc.martincao.rentigo.rentigobackend.payment.model.PaymentType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付响应 DTO
 */
@Data
@NoArgsConstructor
public class PaymentResponseDTO {
    private Long id;
    private Long rentalId;
    private Long userId;
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentStatus status;
    private String paymentMethod;
    private String stripeSessionId;
    private String transactionId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
}
