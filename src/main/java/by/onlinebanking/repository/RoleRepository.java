package by.onlinebanking.repository;

import by.onlinebanking.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.name = :name AND r.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
}
