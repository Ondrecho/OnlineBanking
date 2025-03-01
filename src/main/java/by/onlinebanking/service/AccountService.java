package by.onlinebanking.service;

import by.onlinebanking.dto.AccountDto;
import by.onlinebanking.dto.ResponseDto;
import by.onlinebanking.dto.TransactionRequestDto;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.User;
import by.onlinebanking.model.enums.AccountStatus;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.repository.UserRepository;
import by.onlinebanking.utils.IbanGenerator;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public List<AccountDto> getAccountsByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accounts.stream().map(AccountDto::new).toList();
    }

    public AccountDto createAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setIban(IbanGenerator.generateIban());
        account.setBalance(0.0);
        account.setStatus(AccountStatus.ACTIVE);

        return new AccountDto(accountRepository.save(account));
    }

    public ResponseDto closeAccount(String iban) {
        Account account = accountRepository.findByIban(iban)
                        .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException("Account is already closed");
        }

        if (account.getBalance() > 0.0) {
            throw new IllegalArgumentException("You cannot close an account with a positive balance.");
        }

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);

        return new ResponseDto("Account is closed", LocalDateTime.now(), HttpStatus.OK);
    }

    public ResponseDto processTransaction(TransactionRequestDto transactionRequest) {
        return switch (transactionRequest.getTransactionType()) {
            case DEPOSIT -> deposit(transactionRequest.getIban(), transactionRequest.getAmount());
            case WITHDRAWAL -> withdraw(transactionRequest.getIban(),
                                        transactionRequest.getAmount());
            case TRANSFER -> transfer(transactionRequest.getFromIban(),
                                      transactionRequest.getToIban(),
                                      transactionRequest.getAmount());
            default -> throw new IllegalArgumentException("Invalid operation type");
        };
    }

    public ResponseDto deposit(String iban, Double amount) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        return new ResponseDto("Deposit success: +" + amount,
                                LocalDateTime.now(),
                                HttpStatus.OK);
    }

    public ResponseDto withdraw(String iban, Double amount) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (amount <= 0 || account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds for withdraw");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
        return new ResponseDto("Withdrawal success: -" + amount,
                LocalDateTime.now(),
                HttpStatus.OK);
    }

    public ResponseDto transfer(String fromIban, String toIban, Double amount) {
        if (fromIban.equals(toIban)) {
            throw new IllegalArgumentException("Cannot transfer from account to the same account");
        }

        Account fromAccount = accountRepository.findByIban(fromIban)
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));

        Account toAccount = accountRepository.findByIban(toIban)
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));

        if (amount <= 0 || amount > fromAccount.getBalance()) {
            throw new IllegalArgumentException("Insufficient funds for transfer");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return new ResponseDto("Transfer successful" + amount + " from " + fromIban + " to " + toIban,
                LocalDateTime.now(),
                HttpStatus.OK);
    }
}
