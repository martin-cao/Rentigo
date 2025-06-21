package cc.martincao.rentigo.rentigobackend.rental.service.impl;

import cc.martincao.rentigo.rentigobackend.rental.dto.RentalRequestDTO;
import cc.martincao.rentigo.rentigobackend.rental.dto.RentalResponseDTO;
import cc.martincao.rentigo.rentigobackend.rental.exception.RentalBusinessException;
import cc.martincao.rentigo.rentigobackend.rental.model.Rental;
import cc.martincao.rentigo.rentigobackend.rental.model.RentalStatus;
import cc.martincao.rentigo.rentigobackend.rental.repository.RentalRepository;
import cc.martincao.rentigo.rentigobackend.rental.service.RentalService;
import cc.martincao.rentigo.rentigobackend.user.User;
import cc.martincao.rentigo.rentigobackend.user.repository.UserRepository;
import cc.martincao.rentigo.rentigobackend.vehicle.model.Vehicle;
import cc.martincao.rentigo.rentigobackend.vehicle.model.VehicleStatus;
import cc.martincao.rentigo.rentigobackend.vehicle.repository.VehicleRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public RentalServiceImpl(RentalRepository rentalRepository,
                            VehicleRepository vehicleRepository,
                            UserRepository userRepository) {
        this.rentalRepository = rentalRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public RentalResponseDTO createRental(RentalRequestDTO request, Long userId) {
        // 验证用户存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 验证车辆存在且不在维护中
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        
        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
            throw new RentalBusinessException("Vehicle is under maintenance and not available");
        }

        // 检查车辆是否有时间段冲突的租赁
        List<RentalStatus> activeStatuses = Arrays.asList(
                RentalStatus.PENDING_PAYMENT, 
                RentalStatus.PAID, 
                RentalStatus.ACTIVE
        );
        
        // 获取所有未归还且状态活跃的租赁记录
        List<Rental> activeRentals = rentalRepository.findByVehicleIdAndActualReturnTimeIsNullAndStatusIn(
                request.getVehicleId(), activeStatuses
        );
        
        // 检查时间段是否有冲突
        boolean hasConflict = activeRentals.stream().anyMatch(rental -> 
            !(request.getEndTime().before(rental.getStartTime()) || 
              request.getStartTime().after(rental.getEndTime()))
        );
        
        if (hasConflict) {
            throw new RentalBusinessException("Vehicle is not available for the requested time period");
        }

        // 验证租赁时间
        if (request.getStartTime().after(request.getEndTime())) {
            throw new RentalBusinessException("Start time must be before end time");
        }

        // 允许开始时间比当前时间早5分钟，避免时差问题
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -5);
        Date allowedStartTime = cal.getTime();
        
        if (request.getStartTime().before(allowedStartTime)) {
            throw new RentalBusinessException("Start time cannot be more than 5 minutes in the past");
        }

        // 计算租期天数和总金额
        long diffInMillies = request.getEndTime().getTime() - request.getStartTime().getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        if (diffInDays < 1) {
            diffInDays = 1; // 最少租一天
        }
        BigDecimal totalAmount = vehicle.getDailyPrice().multiply(new BigDecimal(diffInDays));

        // 创建租赁订单
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setVehicle(vehicle);
        rental.setStartTime(request.getStartTime());
        rental.setEndTime(request.getEndTime());
        rental.setTotalAmount(totalAmount);
        rental.setStatus(RentalStatus.PENDING_PAYMENT);

        // 保存租赁订单
        rental = rentalRepository.save(rental);

        return convertToResponseDTO(rental);
    }

    @Override
    @Transactional
    public RentalResponseDTO returnRental(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        // 验证租赁属于当前用户
        if (!rental.getUser().getId().equals(userId)) {
            throw new RentalBusinessException("You can only return your own rentals");
        }

        // 验证租赁状态 - 允许PAID和ACTIVE状态归还
        if (rental.getStatus() != RentalStatus.ACTIVE && rental.getStatus() != RentalStatus.PAID) {
            throw new RentalBusinessException("Rental must be in ACTIVE or PAID status to return");
        }

        // 设置实际归还时间
        rental.setActualReturnTime(new Date());
        rental.setStatus(RentalStatus.FINISHED);

        // 计算逾期费用和是否需要额外支付
        boolean needsAdditionalPayment = false;
        BigDecimal additionalAmount = BigDecimal.ZERO;
        
        if (rental.getActualReturnTime().after(rental.getEndTime())) {
            // 计算逾期时间（小时）
            long overdueMillis = rental.getActualReturnTime().getTime() - rental.getEndTime().getTime();
            long overdueHours = TimeUnit.HOURS.convert(overdueMillis, TimeUnit.MILLISECONDS);
            
            if (overdueHours > 6) {
                // 超过6小时需要补交费用
                needsAdditionalPayment = true;
                
                // 不满一小时按一小时计算
                // 计算去掉6小时免费期后的逾期毫秒数
                long overdueMillisAfterFree = rental.getActualReturnTime().getTime() - rental.getEndTime().getTime() - TimeUnit.HOURS.toMillis(6);
                // 向上取整到小时（不满一小时按一小时计算）
                long chargeableHoursCeilUp = (overdueMillisAfterFree + TimeUnit.HOURS.toMillis(1) - 1) / TimeUnit.HOURS.toMillis(1);
                
                BigDecimal dailyPrice = rental.getVehicle().getDailyPrice();
                // 每小时基础价格 = 每日价格 ÷ 24
                BigDecimal hourlyBasePrice = dailyPrice.divide(new BigDecimal("24"), 2, java.math.RoundingMode.HALF_UP);
                // 每小时超时价格 = 每小时基础价格 × 2
                BigDecimal hourlyOvertimePrice = hourlyBasePrice.multiply(new BigDecimal("2"));
                
                // 计算总超时费用（使用向上取整的小时数）
                BigDecimal totalOvertimeFee = hourlyOvertimePrice.multiply(new BigDecimal(chargeableHoursCeilUp));
                
                // 每日补交上限 = 每日价格 × 1.5
                BigDecimal dailyFeeLimit = dailyPrice.multiply(new BigDecimal("1.5"));
                
                // 计算实际需要补交的天数（基于向上取整的小时数）
                long overdueDays = (chargeableHoursCeilUp / 24) + (chargeableHoursCeilUp % 24 > 0 ? 1 : 0);
                BigDecimal maxTotalFee = dailyFeeLimit.multiply(new BigDecimal(overdueDays));
                
                // 取较小值作为最终费用
                additionalAmount = totalOvertimeFee.min(maxTotalFee);
                
                // 设置超时费用到租赁记录
                rental.setOvertimeAmount(additionalAmount);
                
                // 更新总金额（这里只是记录，实际支付通过Payment系统处理）
                rental.setTotalAmount(rental.getTotalAmount().add(additionalAmount));
                
                // TODO: 创建新的Payment记录用于补交逾期费用
                // 这里预留接口，待Payment模块完成后实现
                // createOvertimePayment(rental.getId(), additionalAmount, "Overtime return fee");
            }
        }

        // 保存租赁记录
        rental = rentalRepository.save(rental);

        // 如果需要额外支付，记录日志（待Payment模块实现）
        if (needsAdditionalPayment) {
            // 这里可以添加日志或者事件发布
            System.out.println("Rental " + rentalId + " requires additional payment: " + additionalAmount);
        }
        rental = rentalRepository.save(rental);

        // 释放车辆
        Vehicle vehicle = rental.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        return convertToResponseDTO(rental);
    }

    @Override
    public List<RentalResponseDTO> getMyRentals(Long userId) {
        List<Rental> rentals = rentalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return rentals.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RentalResponseDTO> getAllRentals() {
        List<Rental> rentals = rentalRepository.findAllByOrderByCreatedAtDesc();
        return rentals.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RentalResponseDTO forceFinishRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        // 只能强制结束进行中或已支付的租赁
        if (rental.getStatus() != RentalStatus.ACTIVE && rental.getStatus() != RentalStatus.PAID) {
            throw new RentalBusinessException("Can only force finish ACTIVE or PAID rentals");
        }

        rental.setActualReturnTime(new Date());
        rental.setStatus(RentalStatus.FINISHED);
        rental = rentalRepository.save(rental);

        // 释放车辆
        Vehicle vehicle = rental.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        return convertToResponseDTO(rental);
    }

    @Override
    @Transactional
    public RentalResponseDTO activateRental(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        // 验证租赁属于当前用户
        if (!rental.getUser().getId().equals(userId)) {
            throw new RentalBusinessException("You can only activate your own rentals");
        }

        // 验证租赁状态必须是PAID
        if (rental.getStatus() != RentalStatus.PAID) {
            throw new RentalBusinessException("Rental must be in PAID status to activate");
        }

        // 验证激活时间：不能提前借出
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(rental.getStartTime());
        
        // 检查是否在同一天
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now);
        
        if (cal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR) ||
            cal.get(Calendar.DAY_OF_YEAR) != nowCal.get(Calendar.DAY_OF_YEAR)) {
            // 如果不是同一天，检查是否超前
            if (rental.getStartTime().after(now)) {
                throw new RentalBusinessException("Cannot activate rental before the start date");
            }
        }
        
        // 检查是否提前超过4小时
        long timeDiff = now.getTime() - rental.getStartTime().getTime();
        long hoursDiff = TimeUnit.HOURS.convert(timeDiff, TimeUnit.MILLISECONDS);
        
        if (hoursDiff < -4) {
            throw new RentalBusinessException("Cannot activate rental more than 4 hours before start time");
        }

        // 验证车辆状态必须是AVAILABLE
        Vehicle vehicle = rental.getVehicle();
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new RentalBusinessException("Vehicle is not available for activation");
        }

        // 激活租赁
        rental.setStatus(RentalStatus.ACTIVE);
        rental = rentalRepository.save(rental);

        // 更新车辆状态为已租出（重用之前获取的vehicle对象）
        vehicle.setStatus(VehicleStatus.RENTED);
        vehicleRepository.save(vehicle);

        return convertToResponseDTO(rental);
    }

    private RentalResponseDTO convertToResponseDTO(Rental rental) {
        RentalResponseDTO dto = new RentalResponseDTO();
        BeanUtils.copyProperties(rental, dto);
        dto.setUserId(rental.getUser().getId());
        dto.setUsername(rental.getUser().getUsername());
        dto.setVehicleId(rental.getVehicle().getId());
        dto.setVehicleModel(rental.getVehicle().getModel());
        return dto;
    }

    // TODO: 预留方法 - 创建逾期费用支付记录
    // 待Payment模块完成后实现
    /*
    private void createOvertimePayment(Long rentalId, BigDecimal amount, String description) {
        // PaymentRequest paymentRequest = new PaymentRequest();
        // paymentRequest.setRentalId(rentalId);
        // paymentRequest.setAmount(amount);
        // paymentRequest.setDescription(description);
        // paymentRequest.setType(PaymentType.OVERTIME_FEE);
        // 
        // paymentService.createPayment(paymentRequest);
    }
    */
}
