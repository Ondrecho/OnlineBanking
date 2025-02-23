package by.onlinebanking.service;

import by.onlinebanking.model.Account;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.repository.UserRepository;
import java.util.List;
import java.util.UUID;
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

    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account createAccount(Long userId, Account account) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        account.setUser(user);
        account.setAccountNumber(UUID.randomUUID()
                .toString().replace("-", "")
                .substring(0, 16));
        return accountRepository.save(account);
    }

    public Account updateAccount(Long accountId, Account accountDetails) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setBalance(accountDetails.getBalance());
        return accountRepository.save(account);
    }

    public void deleteAccount(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new IllegalArgumentException("Account not found");
        }
        accountRepository.deleteById(accountId);
    }
}
