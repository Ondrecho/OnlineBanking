package by.onlinebanking.dto;

import by.onlinebanking.model.User;
import by.onlinebanking.validation.interfaces.OnPatch;
import by.onlinebanking.validation.interfaces.OnUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateUserDto implements UserBaseDto {
    @NotBlank(groups = {OnUpdate.class}, message = "Full name is required")
    @Size(groups = {OnUpdate.class, OnPatch.class}, min = 2, max = 100,
            message = "Full name must be between 2 and 100 characters")
    @Pattern(groups = {OnUpdate.class, OnPatch.class}, regexp = "^[a-zA-Z\\s]+$",
            message = "Full name must contain only letters and spaces")
    private String fullName;

    @NotBlank(groups = {OnUpdate.class}, message = "Email is required")
    @Email(groups = {OnUpdate.class, OnPatch.class}, message = "Email should be valid")
    private String email;

    @NotNull(groups = {OnUpdate.class}, message = "Date of birth is required")
    @PastOrPresent(groups = {OnUpdate.class, OnPatch.class},
            message = "Date of birth cannot be in the future")
    private LocalDate dateOfBirth;

    @NotEmpty(groups = {OnUpdate.class}, message = "At least one role is required")
    private Set<@Valid RoleDto> roles;

    @Size(groups = {OnUpdate.class, OnPatch.class},
            min = 8,
            message = "Password must be at least 8 characters")
    @Pattern(groups = {OnUpdate.class, OnPatch.class},
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
            message = "Password must contain at least one letter and one number"
    )
    @ToString.Exclude
    private String password;

    public UpdateUserDto() {
        // for JSON serialisation
    }

    public UpdateUserDto(User user) {
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.dateOfBirth = user.getDateOfBirth();
        this.roles = user.getRoles().stream()
                .map(RoleDto::new)
                .collect(Collectors.toSet());
    }
}
