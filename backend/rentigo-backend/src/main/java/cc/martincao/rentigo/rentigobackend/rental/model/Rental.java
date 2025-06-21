package cc.martincao.rentigo.rentigobackend.rental.model;

import cc.martincao.rentigo.rentigobackend.user.User;
import cc.martincao.rentigo.rentigobackend.vehicle.model.Vehicle;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "end_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(name = "actual_return_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualReturnTime;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private RentalStatus status = RentalStatus.PENDING_PAYMENT;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "deposit_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "deposit_status", nullable = false)
    private DepositStatus depositStatus = DepositStatus.NOT_COLLECTED;

    @Column(name = "deposit_paid_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date depositPaidAt;

    @Column(name = "deposit_returned_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date depositReturnedAt;

    @Column(name = "overtime_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeAmount = BigDecimal.ZERO;

    @Version
    private Integer version = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Column(name = "reserved_field1")
    private String reservedField1;

    @Column(name = "reserved_field2")
    private String reservedField2;

    @Column(name = "reserved_field3", precision = 10, scale = 2)
    private BigDecimal reservedField3;

    @Column(name = "reserved_field4")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reservedField4;
}
