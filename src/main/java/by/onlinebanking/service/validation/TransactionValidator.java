package by.onlinebanking.service.validation;

import by.onlinebanking.dto.TransactionRequestDto;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.enums.AccountStatus;
import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.model.enums.TransactionType;
import by.onlinebanking.repository.AccountRepository;
import jakarta.validation.ValidationException;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionValidator {
    private final AccountRepository accountRepository;

    @Autowired
    public TransactionValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void validateTransaction(TransactionRequestDto transactionRequest) {
        validateAccountStatuses(transactionRequest);

        validateAmount(transactionRequest.getAmount());

        validateCurrency(transactionRequest.getCurrency());

        if (transactionRequest.getTransactionType() == TransactionType.TRANSFER) {
            validateTransfer(transactionRequest);
        } else {
            validateAccountOperation(transactionRequest);
        }
    }

    private void validateAccountStatuses(TransactionRequestDto transactionRequest) {
        TransactionType transactionType = transactionRequest.getTransactionType();

        switch (transactionType) {
            case TRANSFER -> {
                validateAccountStatus(transactionRequest.getFromIban(), "Sender account");
                validateAccountStatus(transactionRequest.getToIban(), "Receiver account");
            }
            case DEPOSIT, WITHDRAWAL -> {
                validateAccountStatus(transactionRequest.getIban(), "Account");
            }
            default -> throw new ValidationException("Invalid transaction type");
        }
    }

    private void validateAccountStatus(String iban, String accountType) {
        if (iban == null) {
            throw new ValidationException(accountType + " IBAN cannot be null");
        }

        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new ValidationException(accountType + " not found: " + iban));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new ValidationException(accountType + " is closed: " + iban);
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
    }

    private void validateCurrency(Currency currency) {
        if (currency == null) {
            throw new ValidationException("Currency cannot be null");
        }
    }

    private void validateTransfer(TransactionRequestDto transactionRequest) {
        if (transactionRequest.getFromIban().equals(transactionRequest.getToIban())) {
            throw new ValidationException("Sender and receiver account cannot be the same");
        }

        Account fromAccount = accountRepository.findByIban(transactionRequest.getFromIban())
                .orElseThrow(() -> new ValidationException("Sender account not found"));

        Account toAccount = accountRepository.findByIban(transactionRequest.getToIban())
                .orElseThrow(() -> new ValidationException("Receiver account not found"));

        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new ValidationException("Sender account currency does not match receiver account currency");
        }

        if (!fromAccount.getCurrency().equals(transactionRequest.getCurrency())) {
            throw new ValidationException("Sender account currency does not match transaction currency");
        }

        if (fromAccount.getBalance().compareTo(transactionRequest.getAmount()) < 0) {
            throw new ValidationException("Insufficient funds in sender's account");
        }
    }

    private void validateAccountOperation(TransactionRequestDto transactionRequest) {
        if (transactionRequest.getIban() == null) {
            throw new ValidationException("Iban cannot be empty");
        }

        Account account = accountRepository.findByIban(transactionRequest.getIban())
                .orElseThrow(() -> new ValidationException("Account not found"));

        if (!account.getCurrency().equals(transactionRequest.getCurrency())) {
            throw new ValidationException("Account currency does not match transaction currency");
        }

        if (transactionRequest.getTransactionType() == TransactionType.WITHDRAWAL &&
                account.getBalance().compareTo(transactionRequest.getAmount()) < 0) {
            throw new ValidationException("Insufficient funds");
        }
    }
}
