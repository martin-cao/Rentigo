package cc.martincao.rentigo.rentigobackend.payment.controller;

import cc.martincao.rentigo.rentigobackend.auth.AuthenticationUtil;
import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionRequest;
import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionResponse;
import cc.martincao.rentigo.rentigobackend.payment.dto.PaymentResponseDTO;
import cc.martincao.rentigo.rentigobackend.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment", description = "支付管理 API")
public class PaymentController {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentService paymentService;
    private final AuthenticationUtil authenticationUtil;
    
    public PaymentController(PaymentService paymentService, AuthenticationUtil authenticationUtil) {
        this.paymentService = paymentService;
        this.authenticationUtil = authenticationUtil;
    }
    
    /**
     * 创建支付会话
     */
    @PostMapping("/create-session")
    @Operation(summary = "创建支付会话", description = "创建 Stripe Checkout Session 用于支付")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CreatePaymentSessionResponse> createPaymentSession(
            @Valid @RequestBody CreatePaymentSessionRequest request) {
        
        Long userId = authenticationUtil.getCurrentUserId();
        log.info("Creating payment session for user {} and rental {}", userId, request.getRentalId());
        
        try {
            CreatePaymentSessionResponse response = paymentService.createCheckoutSession(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create payment session", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取支付详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取支付详情", description = "根据 ID 获取支付详情")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        log.info("Getting payment details for id: {}", id);
        PaymentResponseDTO payment = paymentService.getPaymentById(id);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 获取当前用户的所有支付记录
     */
    @GetMapping("/my")
    @Operation(summary = "获取当前用户的支付记录", description = "获取当前登录用户的所有支付记录")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<PaymentResponseDTO>> getMyPayments() {
        Long userId = authenticationUtil.getCurrentUserId();
        log.info("Getting payments for user: {}", userId);
        List<PaymentResponseDTO> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * 处理支付 webhook
     */
    @PostMapping("/webhook")
    @Operation(summary = "处理支付 webhook", description = "处理 Stripe Webhook 事件")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            paymentService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to handle webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // 辅助方法
    private Long getCurrentUserId() {
        return authenticationUtil.getCurrentUserId();
    }
}
