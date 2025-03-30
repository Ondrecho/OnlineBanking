package by.onlinebanking.service;

import by.onlinebanking.dto.*;
import by.onlinebanking.exception.*;
import by.onlinebanking.model.*;
import by.onlinebanking.repository.UserRepository;
import by.onlinebanking.validation.RolesValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RolesValidator rolesValidator;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private UserService usersService;

    private User testUser;
    private CreateUserDto createUserDto;
    private UpdateUserDto updateUserDto;
    private Role role;
    private RoleDto roleDto;
    private Set<Role> roles;

    @BeforeEach
    void setUp() {
        // Setup test data
        roleDto = new RoleDto();
        roleDto.setName("ROLE_USER");

        role = new Role();
        role.setName("ROLE_USER");
        roles = new HashSet<>();
        roles.add(role);

        createUserDto = new CreateUserDto();
        createUserDto.setFullName("Test User");
        createUserDto.setEmail("test@example.com");
        createUserDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        createUserDto.setPassword("password123");
        createUserDto.setRoles(Set.of(roleDto));

        updateUserDto = new UpdateUserDto();
        updateUserDto.setFullName("Updated User");
        updateUserDto.setEmail("updated@example.com");
        updateUserDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        updateUserDto.setPassword("newpassword123");
        updateUserDto.setRoles(Set.of(roleDto));

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testUser.setPassword("password123");
        testUser.setRoles(roles);
    }

    @Test
    void getUserById_UserExists_ReturnsUserResponseDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponseDto result = usersService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getFullName());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> usersService.getUserById(1L));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(1L);
    }


    @Test
    void getUsers_WithFullNameFilter_ReturnsFilteredUsers() {
        List<User> users = Collections.singletonList(testUser);
        when(userRepository.findAll(any(Specification.class))).thenReturn(users);

        List<UserResponseDto> result = usersService.getUsers("Test", null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test User", result.get(0).getFullName());
        verify(userRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void getUsers_WithRoleNameFilter_ReturnsFilteredUsers() {
        List<User> users = Collections.singletonList(testUser);
        when(userRepository.findAll(any(Specification.class))).thenReturn(users);

        List<UserResponseDto> result = usersService.getUsers(null, "ROLE_USER");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test User", result.get(0).getFullName());
        verify(userRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void getUsers_NoResults_ThrowsNotFoundException() {
        when(userRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> usersService.getUsers("Nonexistent", null));
        assertEquals("No users found with the specified criteria", exception.getMessage());
        assertEquals("Nonexistent", exception.getDetails().get("fullName"));
        assertNull(exception.getDetails().get("roleName"));
        verify(userRepository).findAll(any(Specification.class));
    }

    @Test
    void getUserByIban_UserExists_ReturnsUserResponseDto() {
        when(userRepository.findByIban("BY00BANK1234567890")).thenReturn(Optional.of(testUser));

        UserResponseDto result = usersService.getUserByIban("BY00BANK1234567890");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findByIban("BY00BANK1234567890");
    }

    @Test
    void getUserByIban_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findByIban("BY00BANK1234567890")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> usersService.getUserByIban("BY00BANK1234567890"));

        assertEquals("Account not found", exception.getMessage());
        verify(userRepository).findByIban("BY00BANK1234567890");
    }

    @Test
    void createUser_ValidInput_ReturnsCreatedUser() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(rolesValidator.validateAndFindRoles(createUserDto.getRoles())).thenReturn(roles);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto result = usersService.createUser(createUserDto);

        assertNotNull(result);
        assertEquals("Test User", result.getFullName());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_EmailExists_ThrowsBusinessException() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> usersService.createUser(createUserDto));

        assertEquals("User with email already exists", exception.getMessage());
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void fullUpdateUser_ValidInput_ReturnsUpdatedUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(rolesValidator.validateAndFindRoles(updateUserDto.getRoles())).thenReturn(roles);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto result = usersService.fullUpdateUser(1L, updateUserDto);

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void fullUpdateUser_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> usersService.fullUpdateUser(1L, updateUserDto));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(1L);
    }

    @Test
    void fullUpdateUser_EmailExists_ThrowsBusinessException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> usersService.fullUpdateUser(1L, updateUserDto));

        assertEquals("User with email already exists", exception.getMessage());
        verify(userRepository).existsByEmail("updated@example.com");
    }

    @Test
    void partialUpdateUser_ValidInput_ReturnsUpdatedUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(rolesValidator.validateAndFindRoles(updateUserDto.getRoles())).thenReturn(roles);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto result = usersService.partialUpdateUser(1L, updateUserDto);

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void partialUpdateUser_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> usersService.partialUpdateUser(1L, updateUserDto));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(1L);
    }

    @Test
    void updateUser_EmptyRoles_UpdatesWithEmptyRoles() {
        User existingUser = new User();
        existingUser.setRoles(Set.of(role));

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setRoles(Set.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        usersService.fullUpdateUser(1L, updateDto);

        assertTrue(existingUser.getRoles().isEmpty());
    }

    @Test
    void partialUpdateUser_UpdateOnlyFullName_UpdatesOnlyFullName() {
        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setFullName("New Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        usersService.partialUpdateUser(1L, userDto);

        assertEquals("New Name", testUser.getFullName());
        // Проверяем, что другие поля не изменились
        assertEquals("test@example.com", testUser.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    void partialUpdateUser_UpdateOnlyEmail_UpdatesOnlyEmail() {
        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        usersService.partialUpdateUser(1L, userDto);

        assertEquals("new@example.com", testUser.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    void partialUpdateUser_UpdateOnlyDateOfBirth_UpdatesOnlyDateOfBirth() {
        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setDateOfBirth(LocalDate.of(2000, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        usersService.partialUpdateUser(1L, userDto);

        assertEquals(LocalDate.of(2000, 1, 1), testUser.getDateOfBirth());
        verify(userRepository).save(testUser);
    }

    @Test
    void partialUpdateUser_UpdateOnlyPassword_UpdatesOnlyPassword() {
        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setPassword("newpassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        usersService.partialUpdateUser(1L, userDto);

        assertEquals("newpassword", testUser.getPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    void partialUpdateUser_UpdateOnlyRoles_UpdatesOnlyRoles() {
        RoleDto adminRoleDto = new RoleDto();
        adminRoleDto.setName("ADMIN");

        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setRoles(Set.of(adminRoleDto));

        Role adminRole = new Role();
        adminRole.setName("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(rolesValidator.validateAndFindRoles(Set.of(adminRoleDto))).thenReturn(Set.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 5. Вызываем тестируемый метод
        usersService.partialUpdateUser(1L, userDto);

        // 6. Проверяем результаты
        assertEquals(1, testUser.getRoles().size());
        assertEquals("ADMIN", testUser.getRoles().iterator().next().getName());
        verify(userRepository).save(testUser);
    }

    @Test
    void createUsersBulk_ExceedsLimit_ThrowsBusinessException() {
        List<CreateUserDto> largeList = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            CreateUserDto dto = new CreateUserDto();
            dto.setEmail("user" + i + "@example.com");
            largeList.add(dto);
        }

        BusinessException exception = assertThrows(BusinessException.class,
                () -> usersService.createUsersBulk(largeList));

        assertEquals("Bulk operation limit exceeded", exception.getMessage());
        assertEquals(1000, exception.getDetails().get("maxAllowed"));
        assertEquals(1001, exception.getDetails().get("actual"));
    }

    @Test
    void createUsersBulk_ValidInput_ReturnsCreatedUsers() {
        List<CreateUserDto> userDtos = List.of(createUserDto);
        when(userRepository.findExistingEmails(anyList())).thenReturn(Collections.emptyList());
        when(rolesValidator.validateAndFindRoles(anySet())).thenReturn(roles);
        when(userRepository.saveAll(anyList())).thenReturn(List.of(testUser));

        List<UserResponseDto> result = usersService.createUsersBulk(userDtos);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository).findExistingEmails(anyList());
        verify(userRepository).saveAll(anyList());
    }

    @Test
    void createUsersBulk_EmptyList_ThrowsValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> usersService.createUsersBulk(null));

        assertEquals("User list cannot be empty or null", exception.getMessage());
    }

    @Test
    void createUsersBulk_TooManyUsers_ThrowsBusinessException() {
        List<CreateUserDto> userDtos = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            userDtos.add(createUserDto);
        }

        BusinessException exception = assertThrows(BusinessException.class,
                () -> usersService.createUsersBulk(userDtos));

        assertEquals("Bulk operation limit exceeded", exception.getMessage());
    }

    @Test
    void createUsersBulk_WithInvalidRoles_ThrowsBusinessException() {
        CreateUserDto invalidUserDto = new CreateUserDto();
        invalidUserDto.setFullName("Invalid User");
        invalidUserDto.setEmail("invalid@example.com");
        invalidUserDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        invalidUserDto.setPassword("password123");

        RoleDto invalidRoleDto = new RoleDto();
        invalidRoleDto.setName("INVALID_ROLE");
        invalidUserDto.setRoles(Set.of(invalidRoleDto));

        List<CreateUserDto> userDtos = List.of(invalidUserDto);

        when(userRepository.findExistingEmails(anyList())).thenReturn(Collections.emptyList());

        when(rolesValidator.validateAndFindRoles(anySet()))
                .thenThrow(new ValidationException("Role not found")
                        .addDetail("roleName", "INVALID_ROLE"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> usersService.createUsersBulk(userDtos));

        assertEquals("Invalid roles for user: invalid@example.com", exception.getMessage());
        assertEquals("invalid@example.com", exception.getDetails().get("email"));
        assertEquals("Role not found", exception.getDetails().get("error"));

        verify(userRepository).findExistingEmails(anyList());
        verify(rolesValidator).validateAndFindRoles(anySet());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    void createUsersBulk_DuplicateEmails_ThrowsBusinessException() {
        CreateUserDto duplicateDto = new CreateUserDto();
        duplicateDto.setEmail("test@example.com");
        duplicateDto.setFullName("Duplicate User");
        duplicateDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        duplicateDto.setPassword("password123");
        duplicateDto.setRoles(Set.of(roleDto));

        List<CreateUserDto> userDtos = List.of(createUserDto, duplicateDto);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> usersService.createUsersBulk(userDtos));

        assertEquals("Duplicate emails in request", exception.getMessage());
    }

    @Test
    void createUsersBulk_ExistingEmails_ThrowsBusinessException() {
        List<CreateUserDto> userDtos = List.of(createUserDto);
        when(userRepository.findExistingEmails(anyList())).thenReturn(List.of("test@example.com"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> usersService.createUsersBulk(userDtos));

        assertEquals("Some emails already exist", exception.getMessage());
        verify(userRepository).findExistingEmails(anyList());
    }

    @Test
    void deleteUser_UserExists_DeletesUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        usersService.deleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> usersService.deleteUser(1L));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(1L);
    }
}