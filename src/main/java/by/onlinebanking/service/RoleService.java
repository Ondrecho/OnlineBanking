package by.onlinebanking.service;

import by.onlinebanking.dto.RoleDto;
import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.model.Role;
import by.onlinebanking.repository.RoleRepository;
import by.onlinebanking.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository,
                       UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public RoleDto createRole(String roleName) {
        if (roleRepository.existsByName(roleName)) {
            throw new BusinessException("Role already exists")
                    .addDetail("roleName", roleName);
        }

        Role role = new Role();

        role.setName(roleName);

        return new RoleDto(roleRepository.save(role));
    }

    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(RoleDto::new)
                .toList();
    }

    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public RoleDto updateRole(Long roleId, RoleDto roleDto) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found")
                        .addDetail("roleId", roleId));

        String newRoleName = roleDto.getName();

        if (roleRepository.existsByNameAndIdNot(newRoleName, roleId)) {
            throw new BusinessException("Role already exists")
                    .addDetail("roleName", newRoleName);
        }

        role.setName(newRoleName);
        Role updatedRole = roleRepository.save(role);

        return new RoleDto(updatedRole);
    }

    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found")
                        .addDetail("roleId", roleId));

        if (userRepository.existsByRolesId(roleId)) {
            throw new BusinessException("Cannot delete role with assigned users")
                    .addDetail("usersCount", userRepository.countByRolesId(roleId));
        }

        roleRepository.delete(role);
    }
}
