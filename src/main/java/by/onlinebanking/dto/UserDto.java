package by.onlinebanking.dto;

import by.onlinebanking.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String fullName;
    private String email;
    private Date dateOfBirth;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
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