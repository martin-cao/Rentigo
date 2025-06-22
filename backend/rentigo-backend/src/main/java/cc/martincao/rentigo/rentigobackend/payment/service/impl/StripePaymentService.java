package cc.martincao.rentigo.rentigobackend.payment.service.impl;

import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionRequest;
import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionResponse;
import cc.martincao.rentigo.rentigobackend.payment.dto.PaymentResponseDTO;
import cc.martincao.rentigo.rentigobackend.payment.exception.PaymentBusinessException;
import cc.martincao.rentigo.rentigobackend.payment.model.Payment;
import cc.martincao.rentigo.rentigobackend.payment.model.PaymentStatus;
import cc.martincao.rentigo.rentigobackend.payment.model.PaymentType;
import cc.martincao.rentigo.rentigobackend.payment.repository.PaymentRepository;
import cc.martincao.rentigo.rentigobackend.payment.service.PaymentService;
import cc.martincao.rentigo.rentigobackend.rental.model.Rental;
import cc.martincao.rentigo.rentigobackend.rental.model.RentalStatus;
import cc.martincao.rentigo.rentigobackend.rental.repository.RentalRepository;
import cc.martincao.rentigo.rentigobackend.user.User;
import cc.martincao.rentigo.rentigobackend.user.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
public class StripePaymentService implements PaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(StripePaymentService.class);
    
    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;
    
    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final String frontendSuccessUrl;
    private final String frontendCancelUrl;
    private final ModelMapper modelMapper;
    
    public StripePaymentService(
            PaymentRepository paymentRepository,
            RentalRepository rentalRepository,
            UserRepository userRepository,
            ModelMapper modelMapper,
            @Value("${app.frontend.success-url}") String frontendSuccessUrl,
            @Value("${app.frontend.cancel-url}") String frontendCancelUrl,
            @Value("${stripe.secret-key}") String stripeSecretKey) {
        this.paymentRepository = paymentRepository;
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.frontendSuccessUrl = frontendSuccessUrl;
        this.frontendCancelUrl = frontendCancelUrl;
        
        // Initialize Stripe API key
        Stripe.apiKey = stripeSecretKey;
    }
    
    @Override
    @Transactional
    public CreatePaymentSessionResponse createCheckoutSession(CreatePaymentSessionRequest request, Long userId) {
        log.info("Creating Stripe checkout session for rental {} by user {}", request.getRentalId(), userId);
        
        try {
            // 1. 验证租赁和用户
            Rental rental = rentalRepository.findById(request.getRentalId())
                    .orElseThrow(() -> new PaymentBusinessException("Rental not found"));
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new PaymentBusinessException("User not found"));
            
            if (!rental.getUser().getId().equals(userId)) {
                throw new PaymentBusinessException("You can only pay for your own rentals");
            }
            
            // 2. 创建支付记录
            Payment payment = new Payment();
            payment.setRental(rental);
            payment.setUser(user);
            payment.setAmount(request.getAmount());
            payment.setPaymentType(request.getPaymentType());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setDescription(request.getDescription());
            payment = paymentRepository.save(payment);
            
            // 3. 创建Stripe Session
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(frontendSuccessUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendCancelUrl)
                    .setClientReferenceId(payment.getId().toString())  // 使用 payment ID 作为参考ID
                    .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                            .putMetadata("payment_id", payment.getId().toString())
                            .putMetadata("rental_id", rental.getId().toString())
                            .putMetadata("payment_type", request.getPaymentType().toString())
                            .build()
                    );
            
            // 添加付款项目
            builder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(request.getDescription())
                                    .build()
                            )
                            .setUnitAmount(request.getAmount().multiply(new java.math.BigDecimal("100")).longValue())
                            .build()
                    )
                    .setQuantity(1L)
                    .build()
            );
            
            SessionCreateParams params = builder.build();
            Session session = Session.create(params);
            
            // 4. 更新支付记录，添加Stripe会话ID
            payment.setStripeSessionId(session.getId());
            payment = paymentRepository.save(payment);
            
            // 5. 返回响应
            return CreatePaymentSessionResponse.builder()
                .sessionId(session.getId())
                .checkoutUrl(session.getUrl())
                .payment(modelMapper.map(payment, PaymentResponseDTO.class))
                .message("Payment session created successfully")
                .build();
            
        } catch (StripeException e) {
            log.error("Failed to create Stripe session", e);
            throw new PaymentBusinessException("Failed to create Stripe payment session: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to create checkout session", e);
            throw new PaymentBusinessException("Failed to create payment session: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        log.info("Received Stripe webhook");
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            String eventType = event.getType();
            log.info("Processing Stripe webhook event type: {}", eventType);
            
            switch (eventType) {
                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "charge.succeeded":
                    handleChargeSucceeded(event);
                    break;
                default:
                    log.info("Ignoring event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error handling Stripe webhook: {}", e.getMessage(), e);
            throw new PaymentBusinessException("Error processing webhook: " + e.getMessage());
        }
    }
    
    @Transactional
    protected void handleCheckoutSessionCompleted(Event event) {
        if (!event.getDataObjectDeserializer().getObject().isPresent()) {
            throw new PaymentBusinessException("Could not deserialize event data");
        }
        
        Session session = (Session) event.getDataObjectDeserializer().getObject().get();
        String sessionId = session.getId();
        log.info("Processing checkout.session.completed for session ID: {}", sessionId);
        
        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new PaymentBusinessException("Payment not found for session: " + sessionId));
        
        updatePaymentAndRentalStatus(payment, session);
    }
    
    @Transactional
    protected void handlePaymentIntentSucceeded(Event event) {
        if (!event.getDataObjectDeserializer().getObject().isPresent()) {
            throw new PaymentBusinessException("Could not deserialize event data");
        }
        
        com.stripe.model.PaymentIntent paymentIntent = 
            (com.stripe.model.PaymentIntent) event.getDataObjectDeserializer().getObject().get();
        
        String paymentId = paymentIntent.getMetadata().get("payment_id");
        if (paymentId == null) {
            log.warn("Payment intent {} does not have payment_id in metadata", paymentIntent.getId());
            return;
        }
        
        Payment payment = paymentRepository.findById(Long.parseLong(paymentId))
                .orElseThrow(() -> new PaymentBusinessException("Payment not found: " + paymentId));
        
        try {
            Session session = Session.retrieve(paymentIntent.getMetadata().get("session_id"));
            updatePaymentAndRentalStatus(payment, session);
        } catch (StripeException e) {
            log.error("Error retrieving session for payment intent: {}", paymentIntent.getId(), e);
            throw new PaymentBusinessException("Failed to process payment intent: " + e.getMessage());
        }
    }
    
    @Transactional
    protected void handleChargeSucceeded(Event event) {
        if (!event.getDataObjectDeserializer().getObject().isPresent()) {
            throw new PaymentBusinessException("Could not deserialize event data");
        }
        
        com.stripe.model.Charge charge = 
            (com.stripe.model.Charge) event.getDataObjectDeserializer().getObject().get();
        
        try {
            String paymentIntentId = charge.getPaymentIntent();
            com.stripe.model.PaymentIntent paymentIntent = 
                com.stripe.model.PaymentIntent.retrieve(paymentIntentId);
            
            String paymentId = paymentIntent.getMetadata().get("payment_id");
            if (paymentId == null) {
                log.warn("Payment intent {} does not have payment_id in metadata", paymentIntent.getId());
                return;
            }
            
            Payment payment = paymentRepository.findById(Long.parseLong(paymentId))
                    .orElseThrow(() -> new PaymentBusinessException("Payment not found: " + paymentId));
            
            Session session = Session.retrieve(paymentIntent.getMetadata().get("session_id"));
            updatePaymentAndRentalStatus(payment, session);
            
            log.info("Successfully processed charge for ID: {}", charge.getId());
        } catch (StripeException e) {
            log.error("Error processing charge: {}", charge.getId(), e);
            throw new PaymentBusinessException("Failed to process charge: " + e.getMessage());
        }
    }
    
    @Transactional
    protected void updatePaymentAndRentalStatus(Payment payment, Session session) {
        log.info("Updating status for payment ID: {} with session ID: {}", payment.getId(), session.getId());
        
        // 1. 检查支付是否已经成功处理
        if (PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            log.info("Payment {} is already marked as successful, skipping update", payment.getId());
            return;
        }
        
        // 2. 更新支付状态
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(session.getPaymentIntent());
        payment.setPaidAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        log.info("Updated payment {} status to SUCCESS", payment.getId());
        
        // 3. 更新租赁状态
        Rental rental = payment.getRental();
        switch (payment.getPaymentType()) {
            case DEPOSIT:
                rental.depositPaid();
                rental.setDepositPaidAt(Date.from(Instant.now()));
                break;
            case RENTAL:
                rental.rentalFeePaid();
                break;
            case OVERTIME:
                rental.overtimeFeePaid();
                break;
        }
        rental = rentalRepository.save(rental);
        log.info("Updated rental {} status after {} payment", rental.getId(), payment.getPaymentType());
    }
    
    @Override
    public List<PaymentResponseDTO> getUserPayments(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentResponseDTO> getRentalPayments(Long rentalId) {
        return paymentRepository.findByRentalIdOrderByCreatedAtDesc(rentalId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PaymentResponseDTO getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new PaymentBusinessException("Payment not found"));
    }
    
    private PaymentResponseDTO convertToDTO(Payment payment) {
        return modelMapper.map(payment, PaymentResponseDTO.class);
    }
}
