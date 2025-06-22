package cc.martincao.rentigo.rentigobackend.vehicle.repository;

import cc.martincao.rentigo.rentigobackend.vehicle.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Byte> {
    // Add custom query methods if needed
}
