package cc.martincao.rentigo.rentigobackend.user.repository;

import cc.martincao.rentigo.rentigobackend.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // Import Optional

public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Find a role by its name
    Optional<Role> findByName(String name);
}
