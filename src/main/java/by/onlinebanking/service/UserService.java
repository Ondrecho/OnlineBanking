package by.onlinebanking.service;

import by.onlinebanking.dto.UserDto;
import by.onlinebanking.model.Role;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.UserRepository;
import by.onlinebanking.service.validation.RolesValidator;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private final UserRepository userRepository;
    private final RolesValidator rolesValidator;

    private final CacheService cacheService;

    @Autowired
    public UserService(UserRepository userRepository,
                       RolesValidator rolesValidator,
                       CacheService cacheService) {
        this.userRepository = userRepository;
        this.rolesValidator = rolesValidator;
        this.cacheService = cacheService;
    }

    public UserDto getUserById(Long id) {
        String cacheKey = "user_" + id;
        return (UserDto) cacheService.get(cacheKey)
                .orElseGet(() -> {
                    LOGGER.info("[DB] Fetching user from database");
                    UserDto result = new UserDto(userRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("User not found")));
                    cacheService.put(cacheKey, result);
                    return result;
                });
    }

    public List<UserDto> getAllUsers() {
        String cacheKey = "all_users";
        return (List<UserDto>) cacheService.get(cacheKey)
                .orElseGet(() -> {
                    LOGGER.info("[DB] Fetching all users from database");
                    List<UserDto> result = userRepository.findAll()
                            .stream()
                            .map(UserDto::new)
                            .toList();
                    cacheService.put(cacheKey, result);
                    return result;
                });
    }

    public List<UserDto> getUsersByName(String fullName) {
        String cacheKey = "users_by_name_" + fullName;
        return (List<UserDto>) cacheService.get(cacheKey)
                .orElseGet(() -> {
                    LOGGER.info("[DB] Fetching users_by_name from database");
                    List<UserDto> result = userRepository.findAllByFullNameLike("%" + fullName + "%")
                            .stream()
                            .map(UserDto::new)
                            .toList();
                    cacheService.put(cacheKey, result);
                    return result;
                });
    }

    public List<UserDto> getUsersByRole(String roleName) {
        String cacheKey = "users_by_role_" + roleName;

        return (List<UserDto>) cacheService.get(cacheKey)
                .orElseGet(() -> {
                    LOGGER.info("[DB] Fetching users_by_role from database");
                    List<UserDto> result = userRepository.findAllByRoleName(roleName)
                            .stream()
                            .map(UserDto::new)
                            .toList();
                    cacheService.put(cacheKey, result);
                    return result;
                });
    }

    public UserDto getUserByIban(String iban) {
        String cacheKey = "user_by_iban_" + iban;

        return (UserDto) cacheService.get(cacheKey)
                .orElseGet(() -> {
                    LOGGER.info("[DB] Fetching users_by_iban from database");
                    UserDto result = new UserDto(userRepository.findByIban(iban)
                            .orElseThrow(() -> new IllegalArgumentException("Account not found")));
                    cacheService.put(cacheKey, result);
                    return result;
                });
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userDto.getId() != null || userDto.getPassword() == null) {
            throw new IllegalArgumentException("Invalid user data");
        }
        User user = new User();

        setFields(userDto, user);

        User savedUser = userRepository.save(user);

        cacheService.evict("all_users");

        return new UserDto(savedUser);
    }

    @Transactional
    public UserDto fullUpdateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot find user"));

        setFields(userDto, user);

        cacheService.invalidateUserCache(id);

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

        cacheService.invalidateUserCache(id);

        return new UserDto(userRepository.save(user));
    }

    @Transactional
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);

        cacheService.clear();

        cacheService.invalidateUserCache(id);

        return true;
    }
}
