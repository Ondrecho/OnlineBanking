package by.onlinebanking.dto;

import by.onlinebanking.model.RoleEnum;
import by.onlinebanking.model.User;
import java.util.Set;

public class UserDto {
    private Long id;
    private String fullName;
    private Set<RoleEnum> roles;

    public UserDto(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.roles = user.getRoles();
    }

    public UserDto() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Set<RoleEnum> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEnum> roles) {
        this.roles = roles;
    }
}
