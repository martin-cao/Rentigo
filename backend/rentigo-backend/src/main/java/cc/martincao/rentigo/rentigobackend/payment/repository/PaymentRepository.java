package cc.martincao.rentigo.rentigobackend.payment.repository;

import cc.martincao.rentigo.rentigobackend.payment.model.Payment;
import cc.martincao.rentigo.rentigobackend.payment.model.PaymentStatus;
import cc.martincao.rentigo.rentigobackend.payment.model.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // 查找某租赁的所有支付记录
    List<Payment> findByRentalIdOrderByCreatedAtDesc(Long rentalId);
    
    // 查找某用户的所有支付记录
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 根据状态查找支付记录
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
    
    // 根据支付类型查找支付记录
    List<Payment> findByPaymentTypeOrderByCreatedAtDesc(PaymentType paymentType);
    
    // 查找某租赁的特定类型支付记录
    List<Payment> findByRentalIdAndPaymentTypeOrderByCreatedAtDesc(Long rentalId, PaymentType paymentType);
    
    // 查找某租赁的成功支付记录
    List<Payment> findByRentalIdAndStatusOrderByCreatedAtDesc(Long rentalId, PaymentStatus status);
}
