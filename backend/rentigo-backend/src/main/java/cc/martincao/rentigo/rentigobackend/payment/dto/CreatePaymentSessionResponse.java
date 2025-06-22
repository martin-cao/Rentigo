package cc.martincao.rentigo.rentigobackend.payment.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 创建支付会话响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentSessionResponse {
    
    private String checkoutUrl;         // Stripe Checkout Session URL
    private String sessionId;           // Stripe Session ID
    private PaymentResponseDTO payment; // 本地支付记录
    private String message;
    
    public CreatePaymentSessionResponse(String sessionId, String checkoutUrl, PaymentResponseDTO payment) {
        this.sessionId = sessionId;
        this.checkoutUrl = checkoutUrl;
        this.payment = payment;
        this.message = "Payment session created successfully";
    }
}
