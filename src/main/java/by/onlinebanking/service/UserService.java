package by.onlinebanking.service;

import by.onlinebanking.dto.UserDto;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.map(UserDto::new).orElse(null);
    }

    public UserDto getUserByName(String name) {
        User user = userRepository.findByName(name);
        return user != null ? new UserDto(user) : null;
    }
}
