package by.onlinebanking.security.service;

import by.onlinebanking.dto.transaction.BaseTransactionDto;
import by.onlinebanking.dto.transaction.SingleAccountTransactionDto;
import by.onlinebanking.dto.transaction.TransferTransactionDto;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountSecurityService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Account validateAndGetAccount(String iban) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException("Account not found")
                        .addDetail("iban", iban));

        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new NotFoundException("User not found")
                .addDetail("email", email));

        if (user.getRoles().stream().noneMatch(r -> r.getName().equals("ROLE_ADMIN")) &&
                !account.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("This operation is available only to administrators");
        }

        return account;
    }

    public boolean canPerformTransaction(BaseTransactionDto transaction, String email) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return true;
        }

        return switch (transaction.getTransactionType()) {
            case DEPOSIT -> true;
            case WITHDRAWAL -> {
                SingleAccountTransactionDto withdrawal = (SingleAccountTransactionDto) transaction;
                yield isAccountOwner(withdrawal.getIban(), user.getId());
            }
            case TRANSFER -> {
                TransferTransactionDto transfer = (TransferTransactionDto) transaction;
                yield isAccountOwner(transfer.getFromIban(), user.getId());
            }
        };
    }

    private boolean isAccountOwner(String iban, Long userId) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException("Account not found")
                        .addDetail("iban", iban));
        return account.getUser().getId().equals(userId);
    }
}