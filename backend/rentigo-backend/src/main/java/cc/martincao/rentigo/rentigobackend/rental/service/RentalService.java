package cc.martincao.rentigo.rentigobackend.rental.service;

import cc.martincao.rentigo.rentigobackend.rental.dto.RentalRequestDTO;
import cc.martincao.rentigo.rentigobackend.rental.dto.RentalResponseDTO;

import java.util.List;

public interface RentalService {
    
    // 创建租赁订单
    RentalResponseDTO createRental(RentalRequestDTO request, Long userId);
    
    // 归还车辆
    RentalResponseDTO returnRental(Long rentalId, Long userId);
    
    // 查询当前用户的租赁记录
    List<RentalResponseDTO> getMyRentals(Long userId);
    
    // 查询所有租赁记录（管理员）
    List<RentalResponseDTO> getAllRentals();
    
    // 强制结束租赁（操作员/管理员）
    RentalResponseDTO forceFinishRental(Long rentalId);
    
    // 激活租赁（从PAID到ACTIVE状态）
    RentalResponseDTO activateRental(Long rentalId, Long userId);
}
