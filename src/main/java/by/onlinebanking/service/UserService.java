package by.onlinebanking.service;

import by.onlinebanking.dto.CreateUserDto;
import by.onlinebanking.dto.UpdateUserDto;
import by.onlinebanking.dto.UserBaseDto;
import by.onlinebanking.dto.UserResponseDto;
import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
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

    public UserResponseDto getUserById(Long id) {
        return new UserResponseDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND)
                        .addDetail(USER_ID, id)));
    }

    @Cacheable(value = "allUsers")
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDto::new)
                .toList();
    }

    @Cacheable(value = "usersByName", key = "#fullName")
    public List<UserResponseDto> getUsersByName(String fullName) {
        return userRepository.findAllByFullNameLike("%" + fullName + "%")
                .stream()
                .map(UserResponseDto::new)
                .toList();
    }

    @Cacheable(value = "usersByRole", key = "#roleName")
    public List<UserResponseDto> getUsersByRole(String roleName) {
        return userRepository.findAllByRoleName(roleName)
                .stream()
                .map(UserResponseDto::new)
                .toList();
    }

    public UserResponseDto getUserByIban(String iban) {
        return new UserResponseDto(userRepository.findByIban(iban)
                            .orElseThrow(() -> new NotFoundException("Account not found")
                                    .addDetail("iban", iban)));
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public UserResponseDto createUser(CreateUserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new BusinessException("User with email already exists")
                    .addDetail("email", userDto.getEmail());
        }

        User user = new User();

        setUserFields(userDto, user);

        User savedUser = userRepository.save(user);

        return new UserResponseDto(savedUser);
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public UserResponseDto fullUpdateUser(Long id, UpdateUserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND)
                        .addDetail(USER_ID, id));

        setUserFields(userDto, user);

        return new UserResponseDto(userRepository.save(user));
    }

    private void setUserFields(UserBaseDto userDto, User user) {
        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setDateOfBirth(userDto.getDateOfBirth());
        user.setPassword(userDto.getPassword());

        Set<Role> roles = rolesValidator.validateAndFindRoles(userDto.getRoles());
        user.setRoles(roles);
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public UserResponseDto partialUpdateUser(Long id, @Validated(OnPatch.class) UpdateUserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND)
                        .addDetail(USER_ID, id));

        updateUserFields(userDto, user);

        return new UserResponseDto(userRepository.save(user));
    }

    private void updateUserFields(UpdateUserDto userDto, User user) {
        if (userDto.getFullName() != null) user.setFullName(userDto.getFullName());
        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());
        if (userDto.getDateOfBirth() != null) user.setDateOfBirth(userDto.getDateOfBirth());
        if (userDto.getPassword() != null) user.setPassword(userDto.getPassword());
        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            Set<Role> validatedRoles = rolesValidator.validateAndFindRoles(userDto.getRoles());
            user.setRoles(validatedRoles);
        }
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
