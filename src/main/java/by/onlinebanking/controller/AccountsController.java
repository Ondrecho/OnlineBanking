package by.onlinebanking.controller;

import by.onlinebanking.dto.AccountDto;
import by.onlinebanking.dto.ResponseDto;
import by.onlinebanking.dto.TransactionRequestDto;
import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.service.AccountService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
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
                                                    @RequestParam Currency currency) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(userId, currency));
    }

    @PutMapping("/{iban}/close")
    public ResponseEntity<ResponseDto> closeAccount(@PathVariable String iban) {
        ResponseDto response = accountService.closeAccount(iban);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{iban}/open")
    public ResponseEntity<ResponseDto> openAccount(@PathVariable String iban) {
        ResponseDto response = accountService.openAccount(iban);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transaction")
    public ResponseEntity<ResponseDto> handleTransaction(@RequestBody TransactionRequestDto request) {
        ResponseDto response = accountService.processTransaction(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{iban}")
    public ResponseEntity<ResponseDto> deleteAccount(@PathVariable String iban) {
        ResponseDto response = accountService.deleteAccount(iban);
        return ResponseEntity.ok(response);
    }
}
