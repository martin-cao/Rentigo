package cc.martincao.rentigo.rentigobackend.vehicle.repository;

import cc.martincao.rentigo.rentigobackend.vehicle.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Short> {
}
