package by.onlinebanking.validation;

import by.onlinebanking.utils.IbanGenerator;
import by.onlinebanking.validation.annotations.IbanFormat;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IbanValidator implements ConstraintValidator<IbanFormat, String> {
    private static final String IBAN_REGEX = "^BY\\d{2}[A-Z]{4}\\d{16}$";

    @Override
    public boolean isValid(String iban, ConstraintValidatorContext context) {
        if (iban == null) return false;
        return iban.matches(IBAN_REGEX) && checkIbanChecksum(iban);
    }

    private boolean checkIbanChecksum(String iban) {
        String checkDigits = iban.substring(2, 4);
        String rawIban = iban.substring(0, 2) + "00" + iban.substring(4);
        String calculatedCheckDigits = IbanGenerator.calculateIbanCheckDigits(rawIban);
        return checkDigits.equals(calculatedCheckDigits);
    }
}
