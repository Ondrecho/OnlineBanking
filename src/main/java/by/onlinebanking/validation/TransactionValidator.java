package by.onlinebanking.validation;

import by.onlinebanking.dto.BaseTransactionDto;
import by.onlinebanking.dto.SingleAccountTransactionDto;
import by.onlinebanking.dto.TransferTransactionDto;
import by.onlinebanking.exception.ValidationException;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.enums.AccountStatus;
import by.onlinebanking.model.enums.TransactionType;
import by.onlinebanking.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionValidator {
    private final AccountRepository accountRepository;

    @Autowired
    public TransactionValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void validateTransaction(BaseTransactionDto transactionRequest) {
        validateAccountStatuses(transactionRequest);

        if (transactionRequest.getTransactionType() == TransactionType.TRANSFER) {
            validateTransfer((TransferTransactionDto) transactionRequest);
        } else {
            validateAccountOperation((SingleAccountTransactionDto) transactionRequest);
        }
    }

    private void validateAccountStatuses(BaseTransactionDto transactionRequest) {
        switch (transactionRequest.getTransactionType()) {
            case TRANSFER -> {
                TransferTransactionDto transferRequest = (TransferTransactionDto) transactionRequest;
                validateAccountStatus(transferRequest.getFromIban(), "Sender account");
                validateAccountStatus(transferRequest.getToIban(), "Receiver account");
            }
            case DEPOSIT, WITHDRAWAL -> {
                SingleAccountTransactionDto singleRequest = (SingleAccountTransactionDto) transactionRequest;
                validateAccountStatus(singleRequest.getIban(), "Account");
            }

            default -> throw new ValidationException("Invalid transaction type")
                    .addDetail("transactionType", transactionRequest.getTransactionType());
        }
    }

    private void validateAccountStatus(String iban, String accountType) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new ValidationException(accountType + " not found: ")
                        .addDetail("iban", iban));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new ValidationException(accountType + " is closed: ")
                    .addDetail("iban", iban);
        }
    }

    private void validateTransfer(TransferTransactionDto transactionRequest) {
        if (transactionRequest.getFromIban().equals(transactionRequest.getToIban())) {
            throw new ValidationException("Sender and receiver account cannot be the same")
                    .addDetail("fromIban", transactionRequest.getFromIban())
                    .addDetail("toIban", transactionRequest.getToIban());
        }

        Account fromAccount = accountRepository.findByIban(transactionRequest.getFromIban())
                .orElseThrow(() -> new ValidationException("Sender account not found")
                .addDetail("fromIban", transactionRequest.getFromIban()));

        Account toAccount = accountRepository.findByIban(transactionRequest.getToIban())
                .orElseThrow(() -> new ValidationException("Receiver account not found")
                .addDetail("toIban", transactionRequest.getToIban()));

        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new ValidationException("Sender account currency does not match receiver account currency")
                    .addDetail("senderCurrency", transactionRequest.getCurrency())
                    .addDetail("receiverCurrency", transactionRequest.getCurrency());
        }

        if (!fromAccount.getCurrency().equals(transactionRequest.getCurrency())) {
            throw new ValidationException("Sender account currency does not match transaction currency")
                    .addDetail("transactionCurrency", transactionRequest.getCurrency());
        }

        if (fromAccount.getBalance().compareTo(transactionRequest.getAmount()) < 0) {
            throw new ValidationException("Insufficient funds in sender's account")
                    .addDetail("amount", transactionRequest.getAmount());
        }
    }

    private void validateAccountOperation(SingleAccountTransactionDto transactionRequest) {
        Account account = accountRepository.findByIban(transactionRequest.getIban())
                .orElseThrow(() -> new ValidationException("Account not found")
                .addDetail("iban", transactionRequest.getIban()));

        if (!account.getCurrency().equals(transactionRequest.getCurrency())) {
            throw new ValidationException("Account currency does not match transaction currency")
                    .addDetail("currency", transactionRequest.getCurrency());
        }

        if (transactionRequest.getTransactionType() == TransactionType.WITHDRAWAL &&
                account.getBalance().compareTo(transactionRequest.getAmount()) < 0) {
            throw new ValidationException("Insufficient funds")
                    .addDetail("amount", transactionRequest.getAmount());
        }
    }
}
