package by.onlinebanking.controller;

import by.onlinebanking.dto.response.OperationResponseDto;
import by.onlinebanking.dto.response.UserResponseDto;
import by.onlinebanking.model.Account;
import by.onlinebanking.security.service.AccountSecurityService;
import by.onlinebanking.service.AccountService;
import by.onlinebanking.service.UserService;
import by.onlinebanking.validation.annotations.IbanFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountsController {
    private final AccountService accountService;
    private final AccountSecurityService accountSecurityService;
    private final UserService userService;

    @Autowired
    public AccountsController(AccountService accountService,
                              AccountSecurityService accountSecurityService,
                              UserService userService) {
        this.accountService = accountService;
        this.accountSecurityService = accountSecurityService;
        this.userService = userService;
    }

    @GetMapping("/{iban}/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserByIban(@PathVariable @IbanFormat String iban) {
        return ResponseEntity.ok(userService.getUserByIban(iban));
    }

    @PatchMapping("/{iban}/close")
    public ResponseEntity<OperationResponseDto> closeAccount(@PathVariable @IbanFormat String iban) {
        Account account = accountSecurityService.validateAndGetAccount(iban);
        OperationResponseDto response = accountService.closeAccount(account);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{iban}/open")
    public ResponseEntity<OperationResponseDto> openAccount(@PathVariable @IbanFormat String iban) {
        Account account = accountSecurityService.validateAndGetAccount(iban);
        OperationResponseDto response = accountService.openAccount(account);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{iban}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OperationResponseDto> deleteAccount(@PathVariable @IbanFormat String iban) {
        OperationResponseDto response = accountService.deleteAccount(iban);
        return ResponseEntity.ok(response);
    }
}
