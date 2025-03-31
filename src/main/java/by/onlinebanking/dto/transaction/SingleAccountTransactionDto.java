package by.onlinebanking.dto.transaction;

import by.onlinebanking.validation.annotations.IbanFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SingleAccountTransactionDto extends BaseTransactionDto {
    @NotBlank(message = "IBAN is required")
    @IbanFormat(message = "Invalid IBAN format")
    private String iban;
}
