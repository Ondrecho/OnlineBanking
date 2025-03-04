package by.onlinebanking.service;

import by.onlinebanking.dto.RoleDto;
import by.onlinebanking.dto.UserDto;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.Role;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.repository.RoleRepository;
import by.onlinebanking.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
    }

    private Set<Role> validateAndFindRoles(Set<RoleDto> roleDtos) {
        return roleDtos.stream()
                .map(roleDto -> {
                    Optional<Role> optionalRole = roleRepository.findByName(roleDto.getName());
                    if (optionalRole.isEmpty()) {
                        throw new IllegalArgumentException("Role not found: " + roleDto.getName());
                    }
                    return optionalRole.get();
                })
                .collect(Collectors.toSet());
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

    public UserDto getUserByIban(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        return new UserDto(account.getUser());
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

        Set<Role> validatedRoles = validateAndFindRoles(userDto.getRoles());
        user.setRoles(validatedRoles);

        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    public Optional<UserDto> updateUser(Long id, UserDto userDto) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();

        if (userDto.getFullName() != null) {
            user.setFullName(userDto.getFullName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getDateOfBirth() != null) {
            user.setDateOfBirth(userDto.getDateOfBirth());
        }
        if (userDto.getPassword() != null) {
            user.setPassword(userDto.getPassword());
        }

        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            Set<Role> validatedRoles = validateAndFindRoles(userDto.getRoles());
            user.setRoles(validatedRoles);
        }

        User savedUser = userRepository.save(user);
        return Optional.of(new UserDto(savedUser));
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
