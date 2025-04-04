package by.onlinebanking.service;

import by.onlinebanking.dto.CreateUserDto;
import by.onlinebanking.dto.UpdateUserDto;
import by.onlinebanking.dto.UserBaseDto;
import by.onlinebanking.dto.UserResponseDto;
import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.exception.ValidationException;
import by.onlinebanking.model.Role;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.UserRepository;
import by.onlinebanking.specifications.UserSpecifications;
import by.onlinebanking.validation.RolesValidator;
import by.onlinebanking.validation.interfaces.OnPatch;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
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

    @Cacheable(value = "users")
    public List<UserResponseDto> getUsers(String fullName, String roleName) {
        Specification<User> spec = Specification.where(null);

        if (fullName != null) {
            spec = spec.and(UserSpecifications.hasFullName(fullName));
        }
        if (roleName != null) {
            spec = spec.and(UserSpecifications.hasRole(roleName));
        }

        List<User> users = userRepository.findAll(spec);

        if (users.isEmpty()) {
            throw new NotFoundException("No users found with the specified criteria")
                    .addDetail("fullName", fullName)
                    .addDetail("roleName", roleName);
        }

        return users.stream()
                .map(UserResponseDto::new)
                .toList();
    }

    public UserResponseDto getUserByIban(String iban) {
        return new UserResponseDto(userRepository.findByIban(iban)
                            .orElseThrow(() -> new NotFoundException("Account not found")
                                    .addDetail("iban", iban)));
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponseDto createUser(CreateUserDto userDto) {
        checkEmail(userDto.getEmail());

        User user = new User();

        setUserFields(userDto, user);

        return new UserResponseDto(userRepository.save(user));
    }

    private void checkEmail(String userDto) {
        if (userRepository.existsByEmail(userDto)) {
            throw new BusinessException("User with email already exists")
                    .addDetail("email", userDto);
        }
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponseDto fullUpdateUser(Long id, UpdateUserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND)
                        .addDetail(USER_ID, id));

        checkEmail(userDto.getEmail());

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
    @CacheEvict(value = "users", allEntries = true)
    public UserResponseDto partialUpdateUser(Long id, @Validated(OnPatch.class) UpdateUserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND)
                        .addDetail(USER_ID, id));

        checkEmail(userDto.getEmail());

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
    @CacheEvict(value = "users", allEntries = true)
    public List<UserResponseDto> createUsersBulk(List<CreateUserDto> userDtos) {
        if (userDtos == null || userDtos.isEmpty()) {
            throw new ValidationException("User list cannot be empty or null");
        }

        if (userDtos.size() > 1000) {
            throw new BusinessException("Bulk operation limit exceeded")
                    .addDetail("maxAllowed", 1000)
                    .addDetail("actual", userDtos.size());
        }

        List<String> requestEmails = userDtos.stream()
                .map(CreateUserDto::getEmail)
                .toList();

        Set<String> duplicateEmails = findDuplicates(requestEmails);
        if (!duplicateEmails.isEmpty()) {
            throw new BusinessException("Duplicate emails in request")
                    .addDetail("duplicates", duplicateEmails);
        }

        List<String> existingEmails = userRepository.findExistingEmails(requestEmails);
        if (!existingEmails.isEmpty()) {
            throw new BusinessException("Some emails already exist")
                    .addDetail("existingEmails", existingEmails);
        }

        List<User> usersToSave = userDtos.stream()
                .map(dto -> {
                    User user = new User();
                    try {
                        setUserFields(dto, user);
                    } catch (ValidationException ex) {
                        throw new BusinessException("Invalid roles for user: " + dto.getEmail())
                                .addDetail("email", dto.getEmail())
                                .addDetail("error", ex.getMessage());
                    }
                    return user;
                })
                .toList();

        List<User> savedUsers = userRepository.saveAll(usersToSave);

        return savedUsers.stream()
                .map(UserResponseDto::new)
                .toList();
    }

    private Set<String> findDuplicates(List<String> emails) {
        Set<String> uniqueEmails = new HashSet<>();
        return emails.stream()
                .filter(email -> !uniqueEmails.add(email))
                .collect(Collectors.toSet());
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND)
                        .addDetail(USER_ID, id));
        userRepository.delete(user);
    }
}
