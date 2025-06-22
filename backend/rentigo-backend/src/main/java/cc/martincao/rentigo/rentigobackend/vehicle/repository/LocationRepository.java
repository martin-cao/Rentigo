package cc.martincao.rentigo.rentigobackend.vehicle.repository;

import cc.martincao.rentigo.rentigobackend.vehicle.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    // Add custom query methods if needed
}
