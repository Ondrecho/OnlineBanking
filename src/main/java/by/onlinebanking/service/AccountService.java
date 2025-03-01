package by.onlinebanking.service;

import by.onlinebanking.dto.AccountDto;
import by.onlinebanking.dto.TransactionRequestDto;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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
        return accounts.stream().map(AccountDto::new).collect(Collectors.toList());
    }

    public AccountDto createAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(UUID.randomUUID()
                .toString().replace("-", "")
                .substring(0, 16));
        account.setBalance(0.0);
        return new AccountDto(accountRepository.save(account));
    }

    public void deleteAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(()->new IllegalArgumentException("Account not found"));
        accountRepository.delete(account);
    }

    public AccountDto processTransaction(TransactionRequestDto transactionRequestDto) {
        return switch (transactionRequestDto.getTransactionType()) {
            case DEPOSIT -> deposit(transactionRequestDto.getAccountId(), transactionRequestDto.getAmount());
            case WITHDRAWAL -> withdraw(transactionRequestDto.getAccountId(),
                                        transactionRequestDto.getAmount());
            case TRANSFER -> {
                transfer(transactionRequestDto.getFromAccountId(),
                         transactionRequestDto.getToAccountId(),
                         transactionRequestDto.getAmount());
                yield null;
            }
            default -> throw new IllegalArgumentException("Invalid operation type");
        };
    }

    public AccountDto deposit(Long accountId, Double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        account.setBalance(account.getBalance() + amount);
        return new AccountDto(accountRepository.save(account));
    }

    public AccountDto withdraw(Long accountId, Double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (amount <= 0 || account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds for withdraw");
        }

        account.setBalance(account.getBalance() - amount);
        return new AccountDto(accountRepository.save(account));
    }

    public void transfer(Long fromAccountId, Long toAccountId, Double amount) {
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer from account to the same account");
        }

        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));

        if (amount <= 0 || amount > fromAccount.getBalance()) {
            throw new IllegalArgumentException("Insufficient funds for transfer");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }
}
