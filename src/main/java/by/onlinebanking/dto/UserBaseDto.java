package by.onlinebanking.dto;

import java.sql.Date;
import java.util.Set;

public interface UserBaseDto {
    String getFullName();

    String getEmail();

    Date getDateOfBirth();

    String getPassword();

    Set<RoleDto> getRoles();
}
