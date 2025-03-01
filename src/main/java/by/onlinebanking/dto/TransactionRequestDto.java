package by.onlinebanking.dto;

import by.onlinebanking.model.TransactionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionRequestDto {
    private TransactionType transactionType;
    private Long accountId;
    private Long fromAccountId;
    private Long toAccountId;
    private Double amount;
}
