package cc.martincao.rentigo.rentigobackend.vehicle.repository;

import cc.martincao.rentigo.rentigobackend.vehicle.model.Vehicle;
import cc.martincao.rentigo.rentigobackend.vehicle.model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByLocationIdAndStatus(Integer locationId, VehicleStatus status);
}
