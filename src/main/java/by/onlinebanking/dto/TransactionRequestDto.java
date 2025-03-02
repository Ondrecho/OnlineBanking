package by.onlinebanking.dto;

import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.model.enums.TransactionType;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionRequestDto {
    private TransactionType transactionType;
    private String iban;
    private String fromIban;
    private String toIban;
    private BigDecimal amount;
    private Currency currency;
}
