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
    private static final String ROLENAME = "roleName";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository,
                       UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public void createRole(String roleName) {
        if (roleRepository.existsByName(roleName)) {
            throw new BusinessException("Role already exists")
                    .addDetail(ROLENAME, roleName);
        }

        Role role = new Role();
        role.setName(roleName);
        roleRepository.save(role);
    }

    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(RoleDto::new)
                .toList();
    }

    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public RoleDto updateRole(String roleName, RoleDto roleDto) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found")
                        .addDetail(ROLENAME, roleName));

        String newRoleName = roleDto.getName();

        if (roleRepository.existsByName(newRoleName)) {
            throw new BusinessException("Role already exists")
                    .addDetail(ROLENAME, newRoleName);
        }

        role.setName(newRoleName);
        Role updatedRole = roleRepository.save(role);

        return new RoleDto(updatedRole);
    }

    public void deleteRole(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found")
                        .addDetail(ROLENAME, roleName));

        if (userRepository.existsByRolesName(roleName)) {
            throw new BusinessException("Cannot delete role with assigned users")
                    .addDetail("usersCount", userRepository.countByRolesName(roleName));
        }

        roleRepository.delete(role);
    }
}
