package by.onlinebanking.dto;

import by.onlinebanking.model.Account;
import by.onlinebanking.model.enums.AccountStatus;
import by.onlinebanking.model.enums.Currency;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDto {
    private Long id;
    private String iban;
    private BigDecimal balance;
    private Currency currency;
    private AccountStatus status;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.iban = account.getIban();
        this.balance = account.getBalance();
        this.currency = account.getCurrency();
        this.status = account.getStatus();
    }
}
