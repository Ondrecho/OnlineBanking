package by.onlinebanking.repository;

import by.onlinebanking.model.User;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @NotNull
    @EntityGraph(attributePaths = {"roles", "accounts"})
    Optional<User> findById(@NotNull Long id);

    @EntityGraph(attributePaths = {"roles", "accounts"})
    List<User> findAllByFullNameLike(String fullName);

    boolean existsByRoles_Name(String roleName);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findAllByRoleName(@Param("roleName") String roleName);

    @Query(nativeQuery = true,
            value = "SELECT u.* FROM users u " +
                    "JOIN user_roles ur ON u.id = ur.user_id " +
                    "JOIN roles r ON ur.role_id = r.id " +
                    "WHERE r.name = :roleName")
    List<User> findAllByRoleNameNative(@Param("roleName") String roleName);
}