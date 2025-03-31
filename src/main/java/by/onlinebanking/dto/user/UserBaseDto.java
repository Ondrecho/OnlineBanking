package by.onlinebanking.dto.user;

import by.onlinebanking.dto.role.RoleDto;
import java.time.LocalDate;
import java.util.Set;

public interface UserBaseDto {
    String getFullName();

    String getEmail();

    LocalDate getDateOfBirth();

    String getPassword();

    Set<RoleDto> getRoles();
}
