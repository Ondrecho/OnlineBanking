package by.onlinebanking.controller;

import by.onlinebanking.dto.response.OperationResponseDto;
import by.onlinebanking.dto.transaction.BaseTransactionDto;
import by.onlinebanking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionsController {
    private final TransactionService transactionService;

    @Autowired
    public TransactionsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @PreAuthorize("@accountSecurityService.canPerformTransaction(#request, authentication.name)")
    public ResponseEntity<OperationResponseDto> handleTransaction(
            @Valid @RequestBody BaseTransactionDto request
    ) {
        OperationResponseDto response = transactionService.processTransaction(request);
        return ResponseEntity.ok(response);
    }
}