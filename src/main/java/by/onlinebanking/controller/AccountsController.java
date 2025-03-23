package by.onlinebanking.controller;

import by.onlinebanking.dto.AccountDto;
import by.onlinebanking.dto.TransactionResponseDto;
import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.service.AccountService;
import by.onlinebanking.validation.annotations.IbanFormat;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountsController {
    private final AccountService accountService;

    @Autowired
    public AccountsController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountDto>> getUserAccounts(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<AccountDto> createAccount(@PathVariable Long userId,
                                                    @NotNull @RequestParam Currency currency) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(userId, currency));
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
