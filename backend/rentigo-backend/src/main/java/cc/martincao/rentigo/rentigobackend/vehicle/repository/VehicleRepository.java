package cc.martincao.rentigo.rentigobackend.vehicle.repository;

import cc.martincao.rentigo.rentigobackend.vehicle.model.Vehicle;
import cc.martincao.rentigo.rentigobackend.vehicle.model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByLocationIdAndStatus(Short locationId, VehicleStatus status);
}
