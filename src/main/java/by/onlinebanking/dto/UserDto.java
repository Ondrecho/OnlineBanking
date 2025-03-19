package by.onlinebanking.dto;

import by.onlinebanking.model.User;
import by.onlinebanking.service.validation.interfaces.OnCreate;
import by.onlinebanking.service.validation.interfaces.OnPatch;
import by.onlinebanking.service.validation.interfaces.OnUpdate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.sql.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class},
            message = "Full name is required")
    @Size(groups = {OnCreate.class, OnUpdate.class, OnPatch.class},
            min = 2, max = 100,
            message = "Full name must be between 2 and 100 characters")
    @Pattern(groups = {OnCreate.class, OnUpdate.class, OnPatch.class},
            regexp = "^[a-zA-Z\\s]+$",
            message = "Full name must contain only letters and spaces")
    private String fullName;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class},
            message = "Email is required")
    @Email(groups = {OnCreate.class, OnUpdate.class, OnPatch.class},
            message = "Email should be valid")
    private String email;

    @NotNull(groups = {OnCreate.class, OnUpdate.class},
            message = "Date of birth is required")
    @PastOrPresent(groups = {OnCreate.class, OnUpdate.class, OnPatch.class},
            message = "Date of birth cannot be in the future")
    private Date dateOfBirth;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(groups = {OnCreate.class}, message = "Password is required")
    @Size(groups = {OnCreate.class, OnUpdate.class, OnPatch.class},
            min = 8,
            message = "Password must be at least 8 characters")
    @Pattern(groups = {OnCreate.class, OnUpdate.class, OnPatch.class},
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
            message = "Password must contain at least one letter and one number"
    )
    private String password;

    @NotEmpty(groups = {OnCreate.class, OnUpdate.class}, message = "At least one role is required")
    private Set<RoleDto> roles = new HashSet<>();

    private Set<AccountDto> accounts = new HashSet<>();

    public UserDto() {}

    public UserDto(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.dateOfBirth = user.getDateOfBirth();
        this.password = user.getPassword();
        this.roles = user.getRoles().stream()
                .map(RoleDto::new)
                .collect(Collectors.toSet());
        this.accounts = user.getAccounts().stream()
                .map(AccountDto::new)
                .collect(Collectors.toSet());
    }
}