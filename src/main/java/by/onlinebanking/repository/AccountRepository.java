package by.onlinebanking.repository;

import by.onlinebanking.model.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    Optional<Account> findByIban(String iban);
}

