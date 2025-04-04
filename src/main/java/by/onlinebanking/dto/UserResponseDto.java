package by.onlinebanking.dto;

import by.onlinebanking.model.User;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private LocalDate dateOfBirth;
    private Set<RoleDto> roles;
    private Set<AccountDto> accounts;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.dateOfBirth = user.getDateOfBirth();
        this.roles = user.getRoles().stream()
                .map(RoleDto::new)
                .collect(Collectors.toSet());
        this.accounts = user.getAccounts().stream()
                .map(AccountDto::new)
                .collect(Collectors.toSet());
    }
}