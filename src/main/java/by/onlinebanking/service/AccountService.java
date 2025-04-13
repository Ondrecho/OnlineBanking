package by.onlinebanking.service;

import by.onlinebanking.dto.account.AccountDto;
import by.onlinebanking.dto.response.OperationResponseDto;
import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.User;
import by.onlinebanking.model.enums.AccountStatus;
import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.repository.UserRepository;
import by.onlinebanking.utils.IbanGenerator;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private static final String ACCOUNT_NOT_FOUND = "Account not found";

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public List<AccountDto> getAccountsByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        if (accounts == null || accounts.isEmpty()) {
            throw new NotFoundException("No accounts found for user")
                    .addDetail("userId", userId);
        }

        return accounts.stream()
                .map(AccountDto::new)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public AccountDto createAccount(Long userId, Currency currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found")
                        .addDetail("userId", userId));

        Account account = new Account();
        account.setUser(user);
        account.setIban(IbanGenerator.generateIban());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency(currency);
        account.setStatus(AccountStatus.ACTIVE);

        return new AccountDto(accountRepository.save(account));
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public OperationResponseDto closeAccount(Account account) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Account is already closed")
                    .addDetail("iban", account.getIban());
        }

//        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
//            throw new BusinessException("You cannot close an account with a positive balance.")
//                    .addDetail("iban", account.getIban());
//        }

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);

        return new OperationResponseDto(
                "Account is closed",
                LocalDateTime.now(),
                HttpStatus.OK
        );
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public OperationResponseDto openAccount(Account account) {
        if (account.getStatus() != AccountStatus.CLOSED) {
            throw new BusinessException("Account is already open or in an invalid state")
                    .addDetail("iban", account.getIban());
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        return new OperationResponseDto(
                "Account is opened",
                LocalDateTime.now(),
                HttpStatus.OK
        );
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public OperationResponseDto deleteAccount(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND)
                        .addDetail("iban", iban));

//        if (account.getStatus() != AccountStatus.CLOSED) {
//            throw new BusinessException("Account is not closed")
//                    .addDetail("iban", iban);
//        }

        accountRepository.delete(account);

        return new OperationResponseDto(
                "Account " + iban + " is deleted",
                LocalDateTime.now(),
                HttpStatus.OK
                );
    }
}