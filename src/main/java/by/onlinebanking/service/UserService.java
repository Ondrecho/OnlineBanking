package by.onlinebanking.service;

import by.onlinebanking.dto.UserCreateUpdateDto;
import by.onlinebanking.dto.UserDto;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.UserRepository;
import java.util.List;
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

    public Optional<UserDto> getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.map(UserDto::new);
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(UserDto::new).toList();
    }

    public List<UserDto> getUserByName(String fullName) {
        List<User> users = userRepository.findAllByFullNameLike("%" + fullName + "%");
        return users.stream().map(UserDto::new).toList();
    }

    public UserDto createUser(UserCreateUpdateDto userCreateUpdateDto) {
        User user = new User();
        user.setFullName(userCreateUpdateDto.getFullName());
        user.setEmail(userCreateUpdateDto.getEmail());
        user.setDateOfBirth(userCreateUpdateDto.getDateOfBirth());
        user.setPassword(userCreateUpdateDto.getPassword());
        user.setRoles(userCreateUpdateDto.getRoles());

        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    public Optional<UserDto> updateUser(Long id, UserCreateUpdateDto userCreateUpdateDto) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        user.setFullName(userCreateUpdateDto.getFullName());
        user.setEmail(userCreateUpdateDto.getEmail());
        user.setDateOfBirth(userCreateUpdateDto.getDateOfBirth());
        user.setPassword(userCreateUpdateDto.getPassword());
        user.setRoles(userCreateUpdateDto.getRoles());

        User savedUser = userRepository.save(user);
        return Optional.of(new UserDto(savedUser));
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}
