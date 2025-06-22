package cc.martincao.rentigo.rentigobackend.payment;

import cc.martincao.rentigo.rentigobackend.payment.model.PaymentType;
import cc.martincao.rentigo.rentigobackend.payment.model.PaymentStatus;
import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionRequest;
import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionResponse;
import cc.martincao.rentigo.rentigobackend.payment.dto.PaymentResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Payment 模块功能验证测试类
 */
@SpringBootTest
public class PaymentModuleVerification {
    
    @Test
    void verifyPaymentTypes() {
        System.out.println("=== Payment Type 验证 ===");
        
        // 测试枚举类
        for (PaymentType type : PaymentType.values()) {
            System.out.println("  - " + type.name() + " (code: " + type.getCode() + ", desc: " + type.getDescription() + ")");
            assertNotNull(type.getDescription());
            assertTrue(type.getCode() >= 0);
        }
    }
    
    @Test
    void verifyPaymentStatus() {
        System.out.println("=== Payment Status 验证 ===");
        
        for (PaymentStatus status : PaymentStatus.values()) {
            System.out.println("  - " + status.name() + " (code: " + status.getCode() + ", desc: " + status.getDescription() + ")");
            assertNotNull(status.getDescription());
            assertTrue(status.getCode() >= 0);
        }
    }
    
    @Test
    void verifyDTOs() {
        System.out.println("=== DTO 验证 ===");
        
        // 测试 CreatePaymentSessionRequest
        CreatePaymentSessionRequest request = new CreatePaymentSessionRequest();
        request.setRentalId(1L);
        request.setPaymentType(PaymentType.RENTAL);
        request.setPaymentMethod("CREDIT_CARD");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("usd");
        
        assertEquals(1L, request.getRentalId());
        assertEquals(PaymentType.RENTAL, request.getPaymentType());
        assertEquals("CREDIT_CARD", request.getPaymentMethod());
        assertEquals(0, new BigDecimal("100.00").compareTo(request.getAmount()));
        assertEquals("usd", request.getCurrency());
        
        // 测试 PaymentResponseDTO
        PaymentResponseDTO payment = new PaymentResponseDTO();
        payment.setId(1L);
        payment.setRentalId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentType(PaymentType.RENTAL);
        payment.setStatus(PaymentStatus.PENDING);
        
        assertEquals(1L, payment.getId());
        assertEquals(1L, payment.getRentalId());
        assertEquals(0, new BigDecimal("100.00").compareTo(payment.getAmount()));
        assertEquals(PaymentType.RENTAL, payment.getPaymentType());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        
        // 测试 CreatePaymentSessionResponse
        CreatePaymentSessionResponse response = new CreatePaymentSessionResponse(
            "cs_test_session_id",
            "https://checkout.stripe.com/pay/cs_test_session_id", 
            payment
        );
        
        assertEquals("cs_test_session_id", response.getSessionId());
        assertEquals("https://checkout.stripe.com/pay/cs_test_session_id", response.getCheckoutUrl());
        assertNotNull(response.getPayment());
        assertEquals(payment.getId(), response.getPayment().getId());
    }
}
