package by.onlinebanking.service;

import by.onlinebanking.dto.AccountDto;
import by.onlinebanking.dto.ResponseDto;
import by.onlinebanking.dto.TransactionRequestDto;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.User;
import by.onlinebanking.model.enums.AccountStatus;
import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.repository.UserRepository;
import by.onlinebanking.service.validation.TransactionValidator;
import by.onlinebanking.utils.IbanGenerator;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionValidator transactionValidator;

    @Autowired
    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository,
                          TransactionValidator transactionValidator) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionValidator = transactionValidator;
    }

    @Cacheable(value = "userAccounts", key = "#userId")
    public List<AccountDto> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(AccountDto::new)
                .toList();
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public AccountDto createAccount(Long userId, Currency currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setIban(IbanGenerator.generateIban());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency(currency);
        account.setStatus(AccountStatus.ACTIVE);

        return new AccountDto(accountRepository.save(account));
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public ResponseDto closeAccount(String iban) {
        Account account = accountRepository.findByIban(iban)
                        .orElseThrow(() -> new IllegalArgumentException("Account was not found"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException("Account is already closed");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("You cannot close an account with a positive balance.");
        }

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);

        return new ResponseDto(
                "Account is closed",
                LocalDateTime.now(),
                HttpStatus.OK
        );
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public ResponseDto openAccount(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new IllegalArgumentException("Account was not found"));

        if (account.getStatus() != AccountStatus.CLOSED) {
            throw new IllegalArgumentException("Account is already open or in an invalid state");
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        return new ResponseDto(
                "Account is opened",
                LocalDateTime.now(),
                HttpStatus.OK
        );
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public ResponseDto deleteAccount(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getStatus() != AccountStatus.CLOSED) {
            throw new IllegalArgumentException("Account is not closed");
        }

        accountRepository.delete(account);

        return new ResponseDto(
                "Account " + iban + " is deleted",
                LocalDateTime.now(),
                HttpStatus.OK
                );
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public ResponseDto processTransaction(TransactionRequestDto transactionRequest) {
        transactionValidator.validateTransaction(transactionRequest);

        return switch (transactionRequest.getTransactionType()) {
            case DEPOSIT -> deposit(transactionRequest.getIban(), transactionRequest.getAmount());
            case WITHDRAWAL -> withdraw(transactionRequest.getIban(),
                                        transactionRequest.getAmount());
            case TRANSFER -> transfer(transactionRequest.getFromIban(),
                                      transactionRequest.getToIban(),
                                      transactionRequest.getAmount());
        };
    }

    @Transactional
    public ResponseDto deposit(String iban, BigDecimal amount) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find account"));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        return new ResponseDto(
                "Deposit success: +" + amount + " " + account.getCurrency(),
                        LocalDateTime.now(),
                        HttpStatus.OK
                );
    }

    @Transactional
    public ResponseDto withdraw(String iban, BigDecimal amount) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0 || account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdraw");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        return new ResponseDto(
                "Withdrawal success: -" + amount + " " + account.getCurrency(),
                LocalDateTime.now(),
                HttpStatus.OK
        );
    }

    @Transactional
    public ResponseDto transfer(String fromIban, String toIban, BigDecimal amount) {
        Account fromAccount = accountRepository.findByIban(fromIban)
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));

        Account toAccount = accountRepository.findByIban(toIban)
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0 || fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for transfer");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return new ResponseDto(
                "Transfer " + amount + " " + fromAccount.getCurrency() +
                " from " + fromIban + " to " + toIban,
                LocalDateTime.now(),
                HttpStatus.OK);
    }
}
