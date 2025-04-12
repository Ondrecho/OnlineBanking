package by.onlinebanking.security.service;

import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws NotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with provided email not found")
                        .addDetail("email", email));
    }
}
