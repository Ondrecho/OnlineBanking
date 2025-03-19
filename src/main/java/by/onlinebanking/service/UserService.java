package by.onlinebanking.service;

import by.onlinebanking.dto.UserDto;
import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.exception.ValidationException;
import by.onlinebanking.model.Role;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.UserRepository;
import by.onlinebanking.service.validation.RolesValidator;
import by.onlinebanking.service.validation.interfaces.OnPatch;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
public class UserService {
    private static final String USER_ID = "userId";
    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final RolesValidator rolesValidator;

    @Autowired
    public UserService(UserRepository userRepository,
                       RolesValidator rolesValidator) {
        this.userRepository = userRepository;
        this.rolesValidator = rolesValidator;
    }

    public UserDto getUserById(Long id) {
        return new UserDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND)
                        .addDetail(USER_ID, id)));
    }

    @Cacheable(value = "allUsers")
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::new)
                .toList();
    }

    @Cacheable(value = "usersByName", key = "#fullName")
    public List<UserDto> getUsersByName(String fullName) {
        return userRepository.findAllByFullNameLike("%" + fullName + "%")
                .stream()
                .map(UserDto::new)
                .toList();
    }

    @Cacheable(value = "usersByRole", key = "#roleName")
    public List<UserDto> getUsersByRole(String roleName) {
        return userRepository.findAllByRoleName(roleName)
                .stream()
                .map(UserDto::new)
                .toList();
    }

    public UserDto getUserByIban(String iban) {
        return new UserDto(userRepository.findByIban(iban)
                            .orElseThrow(() -> new NotFoundException("Account not found")
                                    .addDetail("iban", iban)));
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public UserDto createUser(UserDto userDto) {
        if (userDto.getId() != null) {
            throw new ValidationException("User ID must be null for creation")
                    .addDetail("invalidField", "id");
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new BusinessException("User with email already exists")
                    .addDetail("email", userDto.getEmail());
        }

        User user = new User();

        setFields(userDto, user);

        User savedUser = userRepository.save(user);

        return new UserDto(savedUser);
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public UserDto fullUpdateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND)
                        .addDetail(USER_ID, id));

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
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public UserDto partialUpdateUser(Long id, @Validated(OnPatch.class) UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND)
                        .addDetail(USER_ID, id));

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

        return new UserDto(userRepository.save(user));
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND)
                        .addDetail(USER_ID, id));
        userRepository.delete(user);
    }
}
