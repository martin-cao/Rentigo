package cc.martincao.rentigo.rentigobackend.payment.service;

import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionRequest;
import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionResponse;
import cc.martincao.rentigo.rentigobackend.payment.dto.PaymentResponseDTO;

import java.util.List;

/**
 * 支付服务接口
 */
public interface PaymentService {
    /**
     * 创建支付会话
     */
    CreatePaymentSessionResponse createCheckoutSession(CreatePaymentSessionRequest request, Long userId);
    
    /**
     * 处理 Stripe Webhook
     */
    void handleWebhook(String payload, String sigHeader);
    
    /**
     * 获取指定用户的所有支付记录
     */
    List<PaymentResponseDTO> getUserPayments(Long userId);
    
    /**
     * 获取指定租赁的所有支付记录
     */
    List<PaymentResponseDTO> getRentalPayments(Long rentalId);
    
    /**
     * 根据ID获取支付记录
     */
    PaymentResponseDTO getPaymentById(Long paymentId);
}
