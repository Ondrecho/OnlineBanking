package by.onlinebanking.dto;

import by.onlinebanking.model.Account;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDto {
    private Long id;
    private String iban;
    private Double balance;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.iban = account.getIban();
        this.balance = account.getBalance();
    }
}
