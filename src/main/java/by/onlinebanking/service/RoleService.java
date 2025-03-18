package by.onlinebanking.service;

import by.onlinebanking.dto.RoleDto;
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

    public void createRole(String name) {
        if (roleRepository.existsByName(name)) {
            throw new IllegalArgumentException("Role with name " + name + " already exists");
        }

        Role role = new Role();
        role.setName(name);
        roleRepository.save(role);
    }

    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(RoleDto::new)
                .toList();
    }

    @CacheEvict(value = {"allUsers", "usersByName", "usersByRole"}, allEntries = true)
    public RoleDto updateRole(String name, RoleDto roleDto) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with name: " + name));

        if (roleRepository.existsByName(roleDto.getName())) {
            throw new IllegalArgumentException("Role with name " + roleDto.getName() + " already exists");
        }

        role.setName(roleDto.getName());
        Role updatedRole = roleRepository.save(role);

        return new RoleDto(updatedRole);
    }

    public void deleteRole(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with name: " + name));

        if (userRepository.existsByRolesName(name)) {
            throw new IllegalStateException("Cannot delete role: there are users with role " + name);
        }

        roleRepository.delete(role);
    }
}
