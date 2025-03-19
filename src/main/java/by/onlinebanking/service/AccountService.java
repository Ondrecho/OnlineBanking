package by.onlinebanking.service;

import by.onlinebanking.dto.AccountDto;
import by.onlinebanking.dto.BaseTransactionDto;
import by.onlinebanking.dto.ResponseDto;
import by.onlinebanking.dto.SingleAccountTransactionDto;
import by.onlinebanking.dto.TransferTransactionDto;
import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
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
    private static final String ACCOUNTNOTFOUND = "Account not found";

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
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public ResponseDto closeAccount(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException(ACCOUNTNOTFOUND)
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
                .orElseThrow(() -> new NotFoundException(ACCOUNTNOTFOUND)
                        .addDetail("iban", iban));

        if (account.getStatus() != AccountStatus.CLOSED) {
            throw new BusinessException("Account is already open or in an invalid state")
                    .addDetail("iban", iban);
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
                .orElseThrow(() -> new NotFoundException(ACCOUNTNOTFOUND)
                        .addDetail("iban", iban));

        if (account.getStatus() != AccountStatus.CLOSED) {
            throw new BusinessException("Account is not closed")
                    .addDetail("iban", iban);
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
    public ResponseDto processTransaction(BaseTransactionDto transaction) {
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
    public ResponseDto deposit(String iban, BigDecimal amount) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException(ACCOUNTNOTFOUND)
                        .addDetail("iban", iban));

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
                .orElseThrow(() -> new NotFoundException(ACCOUNTNOTFOUND)
                        .addDetail("iban", iban));

        if (amount.compareTo(BigDecimal.ZERO) <= 0 || account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient funds for withdraw")
                    .addDetail("iban", iban)
                    .addDetail("amount", amount)
                    .addDetail("account", account.getBalance());
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

        return new ResponseDto(
                "Transfer " + amount + " " + fromAccount.getCurrency() +
                " from " + fromIban + " to " + toIban,
                LocalDateTime.now(),
                HttpStatus.OK);
    }
}
