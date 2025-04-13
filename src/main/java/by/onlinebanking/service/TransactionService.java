package by.onlinebanking.service;

import by.onlinebanking.dto.response.OperationResponseDto;
import by.onlinebanking.dto.transaction.BaseTransactionDto;
import by.onlinebanking.dto.transaction.SingleAccountTransactionDto;
import by.onlinebanking.dto.transaction.TransferTransactionDto;
import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.model.Account;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.validation.TransactionValidator;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    private static final String ACCOUNT_NOT_FOUND = "Account not found";

    private final AccountRepository accountRepository;
    private final TransactionValidator transactionValidator;

    @Autowired
    public TransactionService(AccountRepository accountRepository,
                              TransactionValidator transactionValidator) {
        this.accountRepository = accountRepository;
        this.transactionValidator = transactionValidator;
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public OperationResponseDto processTransaction(BaseTransactionDto transaction) {
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
    public OperationResponseDto deposit(String iban, BigDecimal amount) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND)
                        .addDetail("iban", iban));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        return new OperationResponseDto(
                "Deposit success: +" + amount + " " + account.getCurrency(),
                LocalDateTime.now(),
                HttpStatus.OK
        );
    }

    @Transactional
    public OperationResponseDto withdraw(String iban, BigDecimal amount) {
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

        return new OperationResponseDto(
                "Withdrawal success: -" + amount + " " + account.getCurrency(),
                LocalDateTime.now(),
                HttpStatus.OK
        );
    }

    @Transactional
    public OperationResponseDto transfer(String fromIban, String toIban, BigDecimal amount) {
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

        return new OperationResponseDto(
                "Transfer " + amount + " " + fromAccount.getCurrency() +
                        " from " + fromIban + " to " + toIban,
                LocalDateTime.now(),
                HttpStatus.OK);
    }
}
