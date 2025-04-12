package by.onlinebanking.security.service;

import by.onlinebanking.exception.AccessDeniedException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
}