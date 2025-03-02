package by.onlinebanking.service;

import by.onlinebanking.dto.UserDto;
import by.onlinebanking.model.Role;
import by.onlinebanking.model.User;
import by.onlinebanking.model.enums.RoleEnum;
import by.onlinebanking.repository.RoleRepository;
import by.onlinebanking.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDto(user);
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(UserDto::new).toList();
    }

    public List<UserDto> getUserByName(String fullName) {
        List<User> users = userRepository.findAllByFullNameLike("%" + fullName + "%");
        return users.stream().map(UserDto::new).toList();
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userDto.getId() != null || userDto.getPassword() == null) {
            throw new IllegalArgumentException("Invalid user data");
        }
        User user = new User();
        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setDateOfBirth(userDto.getDateOfBirth());
        user.setPassword(userDto.getPassword());
        Set<Role> roles = userDto.getRoles().stream()
                .map(roleDto -> roleRepository.findByName(roleDto.getName())
                        .orElseThrow(() -> new IllegalArgumentException("Role not found " +
                                                                         roleDto.getName())))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        return new UserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto replaceUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User was not found"));

        applyUserUpdates(user, userDto);

        return new UserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUserPartially(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find  user"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "fullName" -> user.setFullName((String) value);
                case "email" -> user.setEmail((String) value);
                case "password" -> user.setPassword((String) value);
                case "dateOdBirth" -> user.setDateOfBirth(Date.valueOf((String) value));
                case "roles" -> {
                    if (!(value instanceof List<?> rolesList)) {
                        throw new IllegalArgumentException("Invalid format for roles");
                    }
                    Set<Role> roles = rolesList.stream()
                            .map(Object::toString)
                            .map(roleName -> roleRepository.findByName(RoleEnum.valueOf(roleName))
                                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " +
                                                                                    roleName)))
                            .collect(Collectors.toSet());
                    user.setRoles(roles);
                }
                default -> throw new IllegalArgumentException("Invalid key: " + key);
            }
        });

        return new UserDto(userRepository.save(user));
    }

    private void applyUserUpdates(User user, UserDto userDto) {
        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setDateOfBirth(userDto.getDateOfBirth());
        user.setPassword(userDto.getPassword());

        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            Set<Role> roles = userDto.getRoles().stream()
                    .map(roleDto -> roleRepository.findByName(roleDto.getName())
                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " +
                                    roleDto.getName())))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
    }

    @Transactional
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}
