package cc.martincao.rentigo.rentigobackend.payment;

import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionRequest;
import cc.martincao.rentigo.rentigobackend.payment.dto.CreatePaymentSessionResponse;
import cc.martincao.rentigo.rentigobackend.payment.model.Payment;
import cc.martincao.rentigo.rentigobackend.payment.model.PaymentStatus;
import cc.martincao.rentigo.rentigobackend.payment.model.PaymentType;
import cc.martincao.rentigo.rentigobackend.payment.repository.PaymentRepository;
import cc.martincao.rentigo.rentigobackend.payment.service.PaymentService;
import cc.martincao.rentigo.rentigobackend.rental.model.Rental;
import cc.martincao.rentigo.rentigobackend.rental.repository.RentalRepository;
import cc.martincao.rentigo.rentigobackend.user.User;
import cc.martincao.rentigo.rentigobackend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RentalRepository rentalRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Rental testRental;

    @BeforeEach
    @Transactional
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        // Create test rental
        testRental = new Rental();
        testRental.setUser(testUser);
        testRental = rentalRepository.save(testRental);
    }

    @Test
    @Transactional
    void testCreatePaymentSession() {
        // Setup payment request
        CreatePaymentSessionRequest request = new CreatePaymentSessionRequest();
        request.setRentalId(testRental.getId());
        request.setPaymentType(PaymentType.DEPOSIT);
        request.setAmount(new BigDecimal("100.00"));

        // Create payment session
        CreatePaymentSessionResponse response = paymentService.createCheckoutSession(request, testUser.getId());

        // Verify response
        assertNotNull(response);
        assertNotNull(response.getSessionId());
        assertNotNull(response.getCheckoutUrl());

        // Verify payment record was created
        Payment payment = paymentRepository.findById(response.getPayment().getId()).orElse(null);
        assertNotNull(payment);
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(testUser.getId(), payment.getUser().getId());
        assertEquals(testRental.getId(), payment.getRental().getId());
        assertEquals(request.getPaymentType(), payment.getPaymentType());
        assertEquals(0, request.getAmount().compareTo(payment.getAmount()));
    }
}
