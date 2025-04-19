package by.onlinebanking.security.model;

import by.onlinebanking.model.User;
import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AuthenticatedUser implements UserDetails {
    private final User user;
    private final boolean isAccountNonLocked;

    public AuthenticatedUser(User user) {
        this.user = user;
        this.isAccountNonLocked = user.getActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }
}
