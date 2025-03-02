package by.onlinebanking.dto;

import by.onlinebanking.model.Role;
import by.onlinebanking.model.enums.RoleEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDto {
    private RoleEnum name;

    public RoleDto() {}

    public RoleDto(Role role) {
        this.name = role.getName();
    }
}
