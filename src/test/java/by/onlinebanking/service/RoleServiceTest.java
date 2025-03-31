package by.onlinebanking.service;

import by.onlinebanking.dto.role.RoleDto;
import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.model.Role;
import by.onlinebanking.repository.RoleRepository;
import by.onlinebanking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void createRole_Success() {
        // Arrange
        String roleName = "ADMIN";
        when(roleRepository.existsByName(roleName)).thenReturn(false);

        Role savedRole = new Role();
        savedRole.setId(1L);
        savedRole.setName(roleName);
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        // Act
        RoleDto result = roleService.createRole(roleName);

        // Assert
        assertNotNull(result);
        assertEquals(roleName, result.getName());
        verify(roleRepository).existsByName(roleName);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void createRole_WhenRoleExists_ThrowsBusinessException() {
        // Arrange
        String roleName = "ADMIN";
        when(roleRepository.existsByName(roleName)).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> roleService.createRole(roleName));

        assertEquals("Role already exists", exception.getMessage());
        assertEquals(roleName, exception.getDetails().get("roleName"));
        verify(roleRepository).existsByName(roleName);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getAllRoles_Success() {
        // Arrange
        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ADMIN");

        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("USER");

        when(roleRepository.findAll()).thenReturn(List.of(role1, role2));

        // Act
        List<RoleDto> result = roleService.getAllRoles();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        assertEquals("USER", result.get(1).getName());
        verify(roleRepository).findAll();
    }

    @Test
    void updateRole_Success() {
        // Arrange
        Long roleId = 1L;
        String newRoleName = "MODERATOR";

        Role existingRole = new Role();
        existingRole.setId(roleId);
        existingRole.setName("ADMIN");

        RoleDto updateDto = new RoleDto();
        updateDto.setName(newRoleName);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByNameAndIdNot(newRoleName, roleId)).thenReturn(false);

        Role updatedRole = new Role();
        updatedRole.setId(roleId);
        updatedRole.setName(newRoleName);
        when(roleRepository.save(existingRole)).thenReturn(updatedRole);

        // Act
        RoleDto result = roleService.updateRole(roleId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(newRoleName, result.getName());
        verify(roleRepository).findById(roleId);
        verify(roleRepository).existsByNameAndIdNot(newRoleName, roleId);
        verify(roleRepository).save(existingRole);
    }

    @Test
    void updateRole_WhenRoleNotFound_ThrowsNotFoundException() {
        // Arrange
        Long roleId = 1L;
        RoleDto updateDto = new RoleDto();
        updateDto.setName("MODERATOR");

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> roleService.updateRole(roleId, updateDto));

        assertEquals("Role not found", exception.getMessage());
        assertEquals(roleId, exception.getDetails().get("roleId"));
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).existsByNameAndIdNot(anyString(), anyLong());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_WhenRoleNameExists_ThrowsBusinessException() {
        // Arrange
        Long roleId = 1L;
        String newRoleName = "MODERATOR";

        Role existingRole = new Role();
        existingRole.setId(roleId);
        existingRole.setName("ADMIN");

        RoleDto updateDto = new RoleDto();
        updateDto.setName(newRoleName);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByNameAndIdNot(newRoleName, roleId)).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> roleService.updateRole(roleId, updateDto));

        assertEquals("Role already exists", exception.getMessage());
        assertEquals(newRoleName, exception.getDetails().get("roleName"));
        verify(roleRepository).findById(roleId);
        verify(roleRepository).existsByNameAndIdNot(newRoleName, roleId);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void deleteRole_Success() {
        // Arrange
        Long roleId = 1L;
        Role role = new Role();
        role.setId(roleId);
        role.setName("ADMIN");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRepository.existsByRolesId(roleId)).thenReturn(false);

        // Act
        roleService.deleteRole(roleId);

        // Assert
        verify(roleRepository).findById(roleId);
        verify(userRepository).existsByRolesId(roleId);
        verify(roleRepository).delete(role);
    }

    @Test
    void deleteRole_WhenRoleNotFound_ThrowsNotFoundException() {
        // Arrange
        Long roleId = 1L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> roleService.deleteRole(roleId));

        assertEquals("Role not found", exception.getMessage());
        assertEquals(roleId, exception.getDetails().get("roleId"));
        verify(roleRepository).findById(roleId);
        verify(userRepository, never()).existsByRolesId(anyLong());
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void deleteRole_WhenUsersAssigned_ThrowsBusinessException() {
        // Arrange
        Long roleId = 1L;
        Role role = new Role();
        role.setId(roleId);
        role.setName("ADMIN");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRepository.existsByRolesId(roleId)).thenReturn(true);
        long usersCount = 5L;
        when(userRepository.countByRolesId(roleId)).thenReturn(usersCount);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> roleService.deleteRole(roleId));

        assertEquals("Cannot delete role with assigned users", exception.getMessage());
        assertEquals(usersCount, exception.getDetails().get("usersCount"));
        verify(roleRepository).findById(roleId);
        verify(userRepository).existsByRolesId(roleId);
        verify(userRepository).countByRolesId(roleId);
        verify(roleRepository, never()).delete(any(Role.class));
    }
}