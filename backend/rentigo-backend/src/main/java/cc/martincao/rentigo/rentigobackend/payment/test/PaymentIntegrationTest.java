package cc.martincao.rentigo.rentigobackend.payment.test;

import cc.martincao.rentigo.rentigobackend.payment.service.PaymentService;
import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionRequest;
import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionResponse;
import cc.martincao.rentigo.rentigobackend.payment.model.PaymentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 支付模块集成测试
 */
@Component
public class PaymentIntegrationTest {
    
    @Autowired
    private PaymentService paymentService;
    
    public void testPaymentModule() {
        System.out.println("=== 支付模块集成测试 ===");
        
        try {
            // 测试创建支付会话
            CreatePaymentSessionRequest request = new CreatePaymentSessionRequest();
            request.setRentalId(1L);
            request.setPaymentType(PaymentType.RENTAL);
            request.setPaymentMethod("CREDIT_CARD");
            request.setAmount(new java.math.BigDecimal("100.00"));
            request.setCurrency("usd");
            request.setSuccessUrl("http://localhost:3000/payment/success");
            request.setCancelUrl("http://localhost:3000/payment/cancel");
            
            CreatePaymentSessionResponse response = paymentService.createCheckoutSession(request, 1L);
            
            System.out.println("✅ 支付会话创建成功:");
            System.out.println("  Session ID: " + response.getSessionId());
            System.out.println("  Checkout URL: " + response.getCheckoutUrl());
            
            // 测试获取用户支付记录
            var userPayments = paymentService.getUserPayments(1L);
            System.out.println("✅ 用户支付记录查询成功，记录数: " + userPayments.size());
            
            // 测试获取租赁支付记录
            var rentalPayments = paymentService.getRentalPayments(1L);
            System.out.println("✅ 租赁支付记录查询成功，记录数: " + rentalPayments.size());
            
            System.out.println("🎉 支付模块集成测试全部通过！");
            
        } catch (Exception e) {
            System.err.println("❌ 支付模块测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
