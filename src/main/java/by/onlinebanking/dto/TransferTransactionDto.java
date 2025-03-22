package by.onlinebanking.dto;

import by.onlinebanking.validation.annotations.IbanFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TransferTransactionDto extends BaseTransactionDto {
    @NotBlank(message = "From IBAN is required")
    @IbanFormat(message = "Invalid From IBAN format")
    private String fromIban;

    @NotBlank(message = "To IBAN is required")
    @IbanFormat(message = "Invalid To IBAN format")
    private String toIban;
}
