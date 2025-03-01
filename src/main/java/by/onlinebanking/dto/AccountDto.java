package by.onlinebanking.dto;

import by.onlinebanking.model.Account;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDto {
    private Long id;
    private String accountNumber;
    private Double balance;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.balance = account.getBalance();
    }
}
