package by.onlinebanking.repository;

import by.onlinebanking.model.User;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @NotNull
    @EntityGraph(attributePaths = {"roles", "accounts"})
    List<User> findAll(Specification<User> spec);

    @NotNull
    @EntityGraph(attributePaths = {"roles", "accounts"})
    Optional<User> findById(@NotNull Long id);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.roles r WHERE r.id = :roleId")
    boolean existsByRolesId(@Param("roleId") Long roleId);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countByRolesId(@Param("roleId") Long roleId);

    @Query("SELECT u FROM User u JOIN u.accounts a WHERE a.iban = :iban")
    Optional<User> findByIban(@Param("iban") String iban);

    @Query("SELECT u.email FROM User u WHERE u.email IN :emails")
    List<String> findExistingEmails(@Param("emails") List<String> emails);
}