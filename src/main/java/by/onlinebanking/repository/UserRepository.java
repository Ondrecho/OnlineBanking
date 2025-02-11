package by.onlinebanking.repository;

import by.onlinebanking.model.User;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @NotNull
    Optional<User> findById(@NotNull Long id);

    User findByName(String name);
}