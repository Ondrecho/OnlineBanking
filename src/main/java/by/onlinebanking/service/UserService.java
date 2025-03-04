package by.onlinebanking.service;

import by.onlinebanking.dto.UserDto;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.Role;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.repository.UserRepository;
import by.onlinebanking.service.validation.RolesValidator;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RolesValidator rolesValidator;

    @Autowired
    public UserService(UserRepository userRepository,
                       AccountRepository accountRepository,
                       RolesValidator rolesValidator) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.rolesValidator = rolesValidator;
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

        setFields(userDto, user);

        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    @Transactional
    public UserDto fullUpdateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot find user"));

        setFields(userDto, user);

        return new UserDto(userRepository.save(user));
    }

    private void setFields(UserDto userDto, User user) {
        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setDateOfBirth(userDto.getDateOfBirth());
        user.setPassword(userDto.getPassword());

        Set<Role> roles = rolesValidator.validateAndFindRoles(userDto.getRoles());
        user.setRoles(roles);
    }

    @Transactional
    public UserDto partialUpdateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
            Set<Role> validatedRoles = rolesValidator.validateAndFindRoles(userDto.getRoles());
            user.setRoles(validatedRoles);
        }

        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
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
