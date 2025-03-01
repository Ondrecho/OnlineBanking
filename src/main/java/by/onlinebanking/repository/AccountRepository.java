package by.onlinebanking.repository;

import by.onlinebanking.model.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);

    Optional<Account> findByIban(String accountNumber);

    @Query("SELECT a.id FROM Account a WHERE a.iban = :iban")
    Optional<Long> findIdByIban(@Param("iban") String iban);
}

