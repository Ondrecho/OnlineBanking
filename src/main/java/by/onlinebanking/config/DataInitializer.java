package by.onlinebanking.config;

import by.onlinebanking.repository.RoleRepository;
import by.onlinebanking.service.RoleService;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final RoleService roleService;

    @Autowired
    public DataInitializer(RoleRepository roleRepository, RoleService roleService) {
        this.roleRepository = roleRepository;
        this.roleService = roleService;
    }

    @Override
    public void run(String... args) {
        Arrays.stream(new String[]{"ROLE_ADMIN", "ROLE_USER"})
                .forEach(this::createRoleIfNotExist);
    }

    private void createRoleIfNotExist(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            roleService.createRole(roleName);
        }
    }
}
