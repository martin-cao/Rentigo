package cc.martincao.rentigo.rentigobackend.payment.dto;

import cc.martincao.rentigo.rentigobackend.payment.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付会话请求 DTO
 */
@Data
public class CreatePaymentSessionRequest {
    
    @NotNull(message = "Rental ID cannot be null")
    private Long rentalId;
    
    @NotNull(message = "Payment type cannot be null")
    private PaymentType paymentType;
    
    @NotNull(message = "Payment method cannot be null")
    private String paymentMethod;
    
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String description;
    
    private String currency = "usd"; // 默认货币类型
    
    // 成功和取消的重定向URL（可选，使用默认值）
    private String successUrl;
    private String cancelUrl;
}
