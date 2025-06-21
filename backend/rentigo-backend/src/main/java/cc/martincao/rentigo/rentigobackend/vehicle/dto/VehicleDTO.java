package cc.martincao.rentigo.rentigobackend.vehicle.dto;

import cc.martincao.rentigo.rentigobackend.vehicle.model.VehicleStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleDTO {
    private Long id;
    private String model;
    private Byte vehicleTypeId;
    private String vehicleTypeName;
    private BigDecimal depositAmount;  // 从车型中获取的押金标准
    private Short locationId;
    private String color;
    private BigDecimal dailyPrice;
    private VehicleStatus status;
    private String licensePlate;
}
