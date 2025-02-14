package by.onlinebanking.dto;

import by.onlinebanking.model.Role;
import by.onlinebanking.model.User;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDto {
    private Long id;
    private String fullName;
    private Set<RoleDto> roles = new HashSet<>();

    public UserDto(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.roles = user.getRoles().stream()
                .map(RoleDto::new)
                .collect(Collectors.toSet());
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

    public Set<RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDto> roles) {
        this.roles = roles;
    }
}
