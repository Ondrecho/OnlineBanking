package by.onlinebanking.dto.user;

import java.time.LocalDate;

public interface UserBaseDto {
    String getFullName();

    String getEmail();

    LocalDate getDateOfBirth();

    String getPassword();
}
