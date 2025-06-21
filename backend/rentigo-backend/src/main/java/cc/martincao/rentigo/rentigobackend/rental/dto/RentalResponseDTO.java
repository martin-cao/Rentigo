package cc.martincao.rentigo.rentigobackend.rental.dto;

import cc.martincao.rentigo.rentigobackend.rental.model.DepositStatus;
import cc.martincao.rentigo.rentigobackend.rental.model.RentalStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RentalResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long vehicleId;
    private String vehicleModel;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date endTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date actualReturnTime;
    
    private RentalStatus status;
    private BigDecimal totalAmount;
    
    // 押金相关字段
    private BigDecimal depositAmount;
    private DepositStatus depositStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date depositPaidAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date depositReturnedAt;
    
    // 超时费用
    private BigDecimal overtimeAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date updatedAt;
}
