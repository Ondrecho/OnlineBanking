package by.onlinebanking.controller;

import by.onlinebanking.dto.response.TransactionResponseDto;
import by.onlinebanking.dto.transaction.BaseTransactionDto;
import by.onlinebanking.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionsController {
    private final AccountService accountService;

    @Autowired
    public TransactionsController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDto> handleTransaction(
            @Valid @RequestBody BaseTransactionDto request
    ) {
        TransactionResponseDto response = accountService.processTransaction(request);
        return ResponseEntity.ok(response);
    }
}