package by.onlinebanking.service.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

public class IbanGenerator {
    private static final String COUNTRY_CODE = "BY";
    private static final String BANK_CODE = "BANK";
    private static final SecureRandom RANDOM = new SecureRandom();

    private IbanGenerator() {}

    public static String generateIban() {
        long accountNumberValue = RANDOM.nextLong() % 10_000_000_000_000_000L;
        accountNumberValue = Math.abs(accountNumberValue);
        String accountNumber = String.format("%016d", accountNumberValue);

        String rawIban = COUNTRY_CODE + "00" + BANK_CODE + accountNumber;
        String checkDigits = calculateIbanCheckDigits(rawIban);

        return COUNTRY_CODE + checkDigits + BANK_CODE + accountNumber;
    }

    public static String calculateIbanCheckDigits(String ibanWithoutCheckDigits) {
        String reformattedIban = ibanWithoutCheckDigits.substring(4) + ibanWithoutCheckDigits.substring(0, 4);
        String numericIban = convertLettersToNumbers(reformattedIban);

        BigInteger ibanNumber = new BigInteger(numericIban);
        int remainder = ibanNumber.mod(BigInteger.valueOf(97)).intValue();

        int checkDigits = 98 - remainder;
        return String.format("%02d", checkDigits);
    }

    private static String convertLettersToNumbers(String input) {
        StringBuilder numericIban = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isLetter(ch)) {
                numericIban.append((ch - 'A' + 10));
            } else {
                numericIban.append(ch);
            }
        }
        return numericIban.toString();
    }
}
