package by.onlinebanking.dto.role;

import by.onlinebanking.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RoleDto {
    private Long id;
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be 2-50 characters")
    @Pattern(regexp = "^ROLE_[A-Z_]+$",
            message = "Role name must start with 'ROLE_' and contain uppercase letters")
    private String name;

    public RoleDto() {
        // for JSON serialization
    }

    public RoleDto(Role role) {
        this.id = role.getId();
        this.name = role.getName();
    }
}
