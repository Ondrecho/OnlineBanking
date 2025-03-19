package by.onlinebanking.dto;

import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.model.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "transactionType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SingleAccountTransactionDto.class, name = "DEPOSIT"),
        @JsonSubTypes.Type(value = SingleAccountTransactionDto.class, name = "WITHDRAWAL"),
        @JsonSubTypes.Type(value = TransferTransactionDto.class, name = "TRANSFER")
})
@ToString
public abstract class BaseTransactionDto {
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private Currency currency;
}
