package by.onlinebanking.service;

import by.onlinebanking.dto.account.AccountDto;
import by.onlinebanking.dto.response.TransactionResponseDto;
import by.onlinebanking.dto.transaction.BaseTransactionDto;
import by.onlinebanking.dto.transaction.SingleAccountTransactionDto;
import by.onlinebanking.dto.transaction.TransferTransactionDto;
import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.User;
import by.onlinebanking.model.enums.AccountStatus;
import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.repository.UserRepository;
import by.onlinebanking.utils.IbanGenerator;
import by.onlinebanking.validation.TransactionValidator;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private static final String ACCOUNT_NOT_FOUND = "Account not found";

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
    public TransactionResponseDto closeAccount(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND)
                        .addDetail("iban", iban));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Account is already closed")
                    .addDetail("iban", iban);
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("You cannot close an account with a positive balance.")
                    .addDetail("iban", iban);
        }

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);

        return new TransactionResponseDto(
                "Account is closed",
                LocalDateTime.now(),
                HttpStatus.OK
        );
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public TransactionResponseDto openAccount(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND)
                        .addDetail("iban", iban));

        if (account.getStatus() != AccountStatus.CLOSED) {
            throw new BusinessException("Account is already open or in an invalid state")
                    .addDetail("iban", iban);
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        return new TransactionResponseDto(
                "Account is opened",
                LocalDateTime.now(),
                HttpStatus.OK
        );
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public TransactionResponseDto deleteAccount(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND)
                        .addDetail("iban", iban));

        if (account.getStatus() != AccountStatus.CLOSED) {
            throw new BusinessException("Account is not closed")
                    .addDetail("iban", iban);
        }

        accountRepository.delete(account);

        return new TransactionResponseDto(
                "Account " + iban + " is deleted",
                LocalDateTime.now(),
                HttpStatus.OK
                );
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public TransactionResponseDto processTransaction(BaseTransactionDto transaction) {
        transactionValidator.validateTransaction(transaction);

        return switch (transaction.getTransactionType()) {
            case DEPOSIT -> {
                SingleAccountTransactionDto depositRequest = (SingleAccountTransactionDto) transaction;
                yield deposit(depositRequest.getIban(), depositRequest.getAmount());
            }
            case WITHDRAWAL -> {
                SingleAccountTransactionDto withdrawalRequest = (SingleAccountTransactionDto) transaction;
                yield withdraw(withdrawalRequest.getIban(), withdrawalRequest.getAmount());
            }
            case TRANSFER -> {
                TransferTransactionDto transfer = (TransferTransactionDto) transaction;
                yield transfer(transfer.getFromIban(), transfer.getToIban(), transfer.getAmount());
            }
        };
    }

    @Transactional
    public TransactionResponseDto deposit(String iban, BigDecimal amount) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND)
                        .addDetail("iban", iban));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        return new TransactionResponseDto(
                "Deposit success: +" + amount + " " + account.getCurrency(),
                        LocalDateTime.now(),
                        HttpStatus.OK
                );
    }

    @Transactional
    public TransactionResponseDto withdraw(String iban, BigDecimal amount) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND)
                        .addDetail("iban", iban));

        if (amount.compareTo(BigDecimal.ZERO) <= 0 || account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient funds for withdraw")
                    .addDetail("iban", iban)
                    .addDetail("amount", amount)
                    .addDetail("account", account.getBalance());
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        return new TransactionResponseDto(
                "Withdrawal success: -" + amount + " " + account.getCurrency(),
                LocalDateTime.now(),
                HttpStatus.OK
        );
    }

    @Transactional
    public TransactionResponseDto transfer(String fromIban, String toIban, BigDecimal amount) {
        Account fromAccount = accountRepository.findByIban(fromIban)
                .orElseThrow(() -> new NotFoundException("Sender account not found")
                        .addDetail("fromIban", fromIban));

        Account toAccount = accountRepository.findByIban(toIban)
                .orElseThrow(() -> new NotFoundException("Receiver account not found")
                        .addDetail("toIban", toIban));

        if (amount.compareTo(BigDecimal.ZERO) <= 0 || fromAccount.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient funds for transfer")
                    .addDetail("fromIban", fromIban)
                    .addDetail("balance", fromAccount.getBalance())
                    .addDetail("amount", amount);
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return new TransactionResponseDto(
                "Transfer " + amount + " " + fromAccount.getCurrency() +
                " from " + fromIban + " to " + toIban,
                LocalDateTime.now(),
                HttpStatus.OK);
    }
}