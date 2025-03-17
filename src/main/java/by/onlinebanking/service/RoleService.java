package by.onlinebanking.service;

import by.onlinebanking.dto.RoleDto;
import by.onlinebanking.model.Role;
import by.onlinebanking.repository.RoleRepository;
import by.onlinebanking.repository.UserRepository;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private static final Logger LOGGER = Logger.getLogger(RoleService.class.getName());
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Autowired
    public RoleService(RoleRepository roleRepository,
                       UserRepository userRepository,
                       CacheService cacheService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    public void createRole(String name) {
        if (roleRepository.existsByName(name)) {
            throw new IllegalArgumentException("Role with name " + name + " already exists");
        }

        Role role = new Role();
        role.setName(name);
        roleRepository.save(role);

        invalidateRolesCache();
    }

    public List<RoleDto> getAllRoles() {
        String cacheKey = "all_roles";
        return (List<RoleDto>) cacheService.get(cacheKey)
                .orElseGet(() -> {
                    LOGGER.info("[DB] Fetching roles from database");
                    List<RoleDto> result = roleRepository.findAll()
                            .stream()
                            .map(RoleDto::new)
                            .toList();
                    cacheService.put(cacheKey, result);
                    return result;
                });
    }

    public RoleDto updateRole(String name, RoleDto roleDto) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with name: " + name));

        if (roleRepository.existsByName(roleDto.getName())) {
            throw new IllegalArgumentException("Role with name " + roleDto.getName() + " already exists");
        }

        role.setName(roleDto.getName());
        Role updatedRole = roleRepository.save(role);

        invalidateRolesCache();

        return new RoleDto(updatedRole);
    }

    public void deleteRole(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with name: " + name));

        if (userRepository.existsByRolesName(name)) {
            throw new IllegalStateException("Cannot delete role: there are users with role " + name);
        }

        invalidateRolesCache();

        roleRepository.delete(role);
    }

    private void invalidateRolesCache() {
        cacheService.evict("all_roles");
    }
}
