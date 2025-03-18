package by.onlinebanking.dto;

import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.model.enums.TransactionType;
import by.onlinebanking.service.validation.IbanFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionRequestDto {
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotBlank(message = "IBAN is required")
    @IbanFormat(message = "Invalid IBAN format")
    private String iban;

    @NotBlank(message = "IBAN is required")
    @IbanFormat(message = "Invalid From IBAN format")
    private String fromIban;

    @NotBlank(message = "IBAN is required")
    @IbanFormat(message = "Invalid To IBAN format")
    private String toIban;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private Currency currency;
}
