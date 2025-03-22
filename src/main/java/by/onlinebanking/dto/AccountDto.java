package by.onlinebanking.dto;

import by.onlinebanking.model.Account;
import by.onlinebanking.model.enums.AccountStatus;
import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.validation.annotations.IbanFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AccountDto {
    private Long id;

    @NotBlank(message = "IBAN is required")
    @Size(min = 16, max = 34, message = "IBAN must be 16-34 characters")
    @IbanFormat(message = "Invalid IBAN format")
    private String iban;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private BigDecimal balance;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Status is required")
    private AccountStatus status;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.iban = account.getIban();
        this.balance = account.getBalance();
        this.currency = account.getCurrency();
        this.status = account.getStatus();
    }
}
