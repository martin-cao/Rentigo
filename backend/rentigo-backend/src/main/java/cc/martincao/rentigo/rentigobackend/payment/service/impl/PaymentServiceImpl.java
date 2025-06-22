package cc.martincao.rentigo.rentigobackend.payment.service.impl;

import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionRequest;
import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionResponse;
import cc.martincao.rentigo.rentigobackend.payment.dto.PaymentResponseDTO;
import cc.martincao.rentigo.rentigobackend.payment.exception.PaymentBusinessException;
import cc.martincao.rentigo.rentigobackend.payment.model.Payment;
import cc.martincao.rentigo.rentigobackend.payment.repository.PaymentRepository;
import cc.martincao.rentigo.rentigobackend.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付服务实现类
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    
    public PaymentServiceImpl(PaymentRepository paymentRepository, ModelMapper modelMapper) {
        this.paymentRepository = paymentRepository;
        this.modelMapper = modelMapper;
    }
    
    @Override
    public CreatePaymentSessionResponse createCheckoutSession(CreatePaymentSessionRequest request, Long userId) {
        log.info("Creating checkout session for rental {} by user {}", request.getRentalId(), userId);
        
        try {
            // 简化版本：返回一个模拟的响应
            return new CreatePaymentSessionResponse(
                "cs_test_mock_session_id", 
                "https://checkout.stripe.com/pay/cs_test_mock_session_id",
                null
            );
        } catch (Exception e) {
            log.error("Failed to create checkout session", e);
            throw new PaymentBusinessException("Failed to create payment session: " + e.getMessage());
        }
    }
    
    @Override
    public void handleWebhook(String payload, String sigHeader) {
        log.info("Handling webhook");
        // TODO: 实现 webhook 处理逻辑
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getUserPayments(Long userId) {
        log.info("Getting payments for user {}", userId);
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return payments.stream()
                .map(payment -> modelMapper.map(payment, PaymentResponseDTO.class))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getRentalPayments(Long rentalId) {
        log.info("Getting payments for rental {}", rentalId);
        List<Payment> payments = paymentRepository.findByRentalIdOrderByCreatedAtDesc(rentalId);
        return payments.stream()
                .map(payment -> modelMapper.map(payment, PaymentResponseDTO.class))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long paymentId) {
        log.info("Getting payment by id {}", paymentId);
        return paymentRepository.findById(paymentId)
                .map(payment -> modelMapper.map(payment, PaymentResponseDTO.class))
                .orElse(null);
    }
}
