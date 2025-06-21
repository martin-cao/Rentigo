package cc.martincao.rentigo.rentigobackend.rental.repository;

import cc.martincao.rentigo.rentigobackend.rental.model.Rental;
import cc.martincao.rentigo.rentigobackend.rental.model.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    
    // 查找某用户的所有租赁记录
    List<Rental> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 查找某车辆当前是否有未完成的租赁（保留原有方法，用于其他地方）
    Optional<Rental> findFirstByVehicleIdAndStatusInOrderByCreatedAtDesc(
            Long vehicleId, List<RentalStatus> activeStatuses);
    
    // 查找所有租赁记录（管理员用）
    List<Rental> findAllByOrderByCreatedAtDesc();
    
    // 根据状态查找租赁记录
    List<Rental> findByStatusOrderByCreatedAtDesc(RentalStatus status);
    
    // 查找指定车辆、未实际归还、且状态为活跃的租赁记录
    List<Rental> findByVehicleIdAndActualReturnTimeIsNullAndStatusIn(
            Long vehicleId, List<RentalStatus> activeStatuses);
}
