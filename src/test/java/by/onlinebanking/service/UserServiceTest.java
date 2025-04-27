package by.onlinebanking.service;

import by.onlinebanking.dto.response.UserResponseDto;
import by.onlinebanking.dto.role.RoleDto;
import by.onlinebanking.dto.user.CreateUserDto;
import by.onlinebanking.dto.user.UpdateUserDto;
import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.exception.ValidationException;
import by.onlinebanking.model.Role;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.RoleRepository;
import by.onlinebanking.repository.UserRepository;
import by.onlinebanking.security.dto.request.RegisterRequest;
import by.onlinebanking.security.model.AuthenticatedUser;
import by.onlinebanking.validation.RolesValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RolesValidator rolesValidator;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UpdateUserDto updateUserDto;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName("ROLE_USER");

        adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testUser.setActive(true);
        testUser.setRoles(Set.of(userRole));

        updateUserDto = new UpdateUserDto();
        updateUserDto.setFullName("Updated User");
        updateUserDto.setEmail("updated@example.com");
        updateUserDto.setDateOfBirth(LocalDate.of(1995, 5, 5));
        updateUserDto.setPassword("newPassword123");
        updateUserDto.setActive(false);
        updateUserDto.setRoles(Set.of(new RoleDto(adminRole)));
    }


    @Test
    @Transactional
    void registerUser_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password");
        request.setFullName("New User");
        request.setDateOfBirth(LocalDate.of(1995, 5, 5));

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        userService.registerUser(request);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @Transactional
    void registerUser_EmailExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.registerUser(request));
    }

    @Test
    @Transactional
    void changePassword_Success() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(testUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        SecurityContextHolder.setContext(securityContext);

        when(passwordEncoder.matches("currentPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newPassword", testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        userService.changePassword("currentPassword", "newPassword", "newPassword");

        assertEquals("newEncodedPassword", testUser.getPassword());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @Transactional
    void changePassword_PasswordMismatch_ThrowsException() {
        assertThrows(ValidationException.class, () ->
                userService.changePassword("current", "new", "different"));
    }

    @Test
    @Transactional
    void changePassword_InvalidCurrentPassword_ThrowsException() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(testUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        SecurityContextHolder.setContext(securityContext);

        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        assertThrows(ValidationException.class, () ->
                userService.changePassword("wrongPassword", "new", "new"));
    }

    @Test
    @Transactional
    void changePassword_SamePassword_ThrowsException() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(testUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        SecurityContextHolder.setContext(securityContext);

        when(passwordEncoder.matches("current", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("new", testUser.getPassword())).thenReturn(true);

        assertThrows(ValidationException.class, () ->
                userService.changePassword("current", "new", "new"));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponseDto response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals("Test User", response.getFullName());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getUsers_Success() {
        Pageable pageable = mock(Pageable.class);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(testUser)));

        Page<UserResponseDto> result = userService.getUsers("Test", List.of("ROLE_USER"), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getUserByIban_Success() {
        when(userRepository.findByIban("IBAN123")).thenReturn(Optional.of(testUser));

        UserResponseDto response = userService.getUserByIban("IBAN123");

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void getUserByIban_NotFound_ThrowsException() {
        when(userRepository.findByIban(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserByIban("INVALID"));
    }

    @Test
    @Transactional
    void createUser_Success() {
        CreateUserDto dto = new CreateUserDto();
        dto.setFullName("New User");
        dto.setEmail("new@example.com");
        dto.setPassword("password");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setActive(true);
        dto.setRoles(Set.of(new RoleDto(userRole)));

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(rolesValidator.validateAndFindRoles(anySet())).thenReturn(Set.of(userRole)); // Исправлено здесь
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto response = userService.createUser(dto);

        assertNotNull(response);
        assertEquals("Test User", response.getFullName());
    }

    @Test
    @Transactional
    void fullUpdateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(rolesValidator.validateAndFindRoles(anySet())).thenReturn(Set.of(adminRole));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto response = userService.fullUpdateUser(1L, updateUserDto);

        assertNotNull(response);
        assertEquals("Updated User", response.getFullName());
        assertEquals("updated@example.com", response.getEmail());
        assertFalse(response.isActive());
        assertEquals("ROLE_ADMIN",response.getRoles().iterator().next().getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @Transactional
    void partialUpdateUser_Success() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setFullName("Partially Updated");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto response = userService.partialUpdateUser(1L, dto);

        assertNotNull(response);
        assertEquals("Partially Updated", testUser.getFullName());
    }


    @Test
    void fullUpdateUser_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.fullUpdateUser(1L, updateUserDto));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(1L);
    }

    @Test
    void fullUpdateUser_EmailExists_ThrowsBusinessException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("updated@example.com", 1L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.fullUpdateUser(1L, updateUserDto));

        assertEquals("Email already taken by another user", exception.getMessage());
        verify(userRepository).existsByEmailAndIdNot("updated@example.com", 1L);
    }

    @Test
    void partialUpdateUser_ValidInput_ReturnsUpdatedUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("updated@example.com", 1L)).thenReturn(false);
        when(rolesValidator.validateAndFindRoles(updateUserDto.getRoles())).thenReturn(Set.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto result = userService.partialUpdateUser(1L, updateUserDto);

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("updated@example.com", 1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void partialUpdateUser_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.partialUpdateUser(1L, updateUserDto));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(1L);
    }

    @Test
    void partialUpdateUser_UpdateOnlyFullName_UpdatesOnlyFullName() {
        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setFullName("New Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.partialUpdateUser(1L, userDto);

        assertEquals("New Name", testUser.getFullName());

        assertEquals("test@example.com", testUser.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    void partialUpdateUser_UpdateOnlyEmail_UpdatesOnlyEmail() {
        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);

        userService.partialUpdateUser(1L, userDto);

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("new@example.com", 1L);
        assertEquals("new@example.com", testUser.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    void partialUpdateUser_UpdateOnlyDateOfBirth_UpdatesOnlyDateOfBirth() {
        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setDateOfBirth(LocalDate.of(2000, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.partialUpdateUser(1L, userDto);

        assertEquals(LocalDate.of(2000, 1, 1), testUser.getDateOfBirth());
        verify(userRepository).save(testUser);
    }

    @Test
    void partialUpdateUser_UpdateOnlyPassword_UpdatesOnlyPassword() {
        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setPassword("NewPassword777");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("NewPassword777");

        userService.partialUpdateUser(1L, userDto);

        assertEquals("NewPassword777", testUser.getPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    void partialUpdateUser_UpdateOnlyRoles_UpdatesOnlyRoles() {
        RoleDto adminRoleDto = new RoleDto();
        adminRoleDto.setName("ADMIN");

        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setRoles(Set.of(adminRoleDto));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(rolesValidator.validateAndFindRoles(Set.of(adminRoleDto))).thenReturn(Set.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.partialUpdateUser(1L, userDto);

        assertEquals(1, testUser.getRoles().size());
        assertEquals("ROLE_ADMIN", testUser.getRoles().iterator().next().getName());
        verify(userRepository).save(testUser);
    }

    @Test
    @Transactional
    void createUsersBulk_Success() {
        CreateUserDto dto1 = new CreateUserDto();
        dto1.setFullName("User 1");
        dto1.setEmail("user1@example.com");
        dto1.setPassword("pass1");
        dto1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto1.setRoles(Set.of(new RoleDto(userRole)));

        CreateUserDto dto2 = new CreateUserDto();
        dto2.setFullName("User 2");
        dto2.setEmail("user2@example.com");
        dto2.setPassword("pass2");
        dto2.setDateOfBirth(LocalDate.of(1991, 2, 2));
        dto2.setRoles(Set.of(new RoleDto(adminRole)));

        when(userRepository.findExistingEmails(anyList())).thenReturn(Collections.emptyList());
        when(rolesValidator.validateAndFindRoles(anySet())).thenReturn(Set.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.saveAll(anyList())).thenReturn(List.of(testUser, testUser));

        List<UserResponseDto> responses = userService.createUsersBulk(List.of(dto1, dto2));

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    @Transactional
    void createUsersBulk_DuplicateEmailsInRequest_ThrowsException() {
        CreateUserDto dto1 = new CreateUserDto();
        dto1.setEmail("duplicate@example.com");

        CreateUserDto dto2 = new CreateUserDto();
        dto2.setEmail("duplicate@example.com");

        List<CreateUserDto> list = List.of(dto1, dto2);

        assertThrows(BusinessException.class, () ->
                userService.createUsersBulk(list));
    }

    @Test
    @Transactional
    void createUsersBulk_ExistingEmails_ThrowsException() {
        CreateUserDto dto = new CreateUserDto();
        dto.setEmail("existing@example.com");
        List<CreateUserDto> list = List.of(dto);
        when(userRepository.findExistingEmails(anyList())).thenReturn(List.of("existing@example.com"));

        assertThrows(BusinessException.class, () ->
                userService.createUsersBulk(list));
    }

    @Test
    @Transactional
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @Transactional
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void getUserFromAuthentication_Success() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(testUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        SecurityContextHolder.setContext(securityContext);

        User user = userService.getUserFromAuthentication();

        assertNotNull(user);
        assertEquals(testUser.getId(), user.getId());
    }
}