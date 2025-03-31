package by.onlinebanking.controller;

import by.onlinebanking.dto.response.TransactionResponseDto;
import by.onlinebanking.dto.response.UserResponseDto;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.service.AccountService;
import by.onlinebanking.service.UserService;
import by.onlinebanking.validation.annotations.IbanFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private final UserService userService;

    @Autowired
    public AccountsController(AccountService accountService,
                              UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping("/{iban}/user")
    public ResponseEntity<UserResponseDto> getUserByIban(@PathVariable @IbanFormat String iban) {
        UserResponseDto response = userService.getUserByIban(iban);
        if (response == null) {
            throw new NotFoundException("No users found with IBAN").addDetail("iban", iban);
        }
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{iban}/close")
    public ResponseEntity<TransactionResponseDto> closeAccount(@PathVariable @IbanFormat String iban) {
        TransactionResponseDto response = accountService.closeAccount(iban);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{iban}/open")
    public ResponseEntity<TransactionResponseDto> openAccount(@PathVariable @IbanFormat String iban) {
        TransactionResponseDto response = accountService.openAccount(iban);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{iban}")
    public ResponseEntity<TransactionResponseDto> deleteAccount(@PathVariable @IbanFormat String iban) {
        TransactionResponseDto response = accountService.deleteAccount(iban);
        return ResponseEntity.ok(response);
    }
}
