package by.onlinebanking.dto;

import by.onlinebanking.service.validation.annotations.IbanFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingleAccountTransactionDto extends BaseTransactionDto {
    @NotBlank(message = "IBAN is required")
    @IbanFormat(message = "Invalid IBAN format")
    private String iban;
}
