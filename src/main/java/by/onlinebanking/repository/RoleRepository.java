package by.onlinebanking.repository;

import by.onlinebanking.model.Role;
import by.onlinebanking.model.enums.RoleEnum;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
}
