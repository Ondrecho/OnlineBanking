package by.onlinebanking.dto;

import by.onlinebanking.model.Role;
import by.onlinebanking.model.User;
import java.util.HashSet;
import java.util.Set;

public class UserDto {
    private Long id;
    private String fullName;
    private Set<Role> roles = new HashSet<>();

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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
