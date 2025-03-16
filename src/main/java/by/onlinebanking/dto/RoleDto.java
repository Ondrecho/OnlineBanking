package by.onlinebanking.dto;

import by.onlinebanking.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDto {
    private String name;

    public RoleDto() {}

    public RoleDto(Role role) {
        this.name = role.getName();
    }
}
