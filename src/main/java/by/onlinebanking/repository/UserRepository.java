package by.onlinebanking.repository;

import by.onlinebanking.model.User;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @NotNull
    @EntityGraph(attributePaths = {"roles", "accounts"})
    Optional<User> findById(@NotNull Long id);

    @EntityGraph(attributePaths = {"roles", "accounts"})
    List<User> findAllByFullNameLike(String fullName);
}