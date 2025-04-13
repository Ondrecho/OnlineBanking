package by.onlinebanking.config;

import by.onlinebanking.model.Role;
import by.onlinebanking.model.User;
import by.onlinebanking.repository.RoleRepository;
import by.onlinebanking.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initAdmin();
    }

    private void initRoles() {
        if (roleRepository.findByName(ROLE_USER).isEmpty()) {
            Role role = new Role();
            role.setName(ROLE_USER);
            roleRepository.save(role);
        }

        if (roleRepository.findByName(ROLE_ADMIN).isEmpty()) {
            Role role = new Role();
            role.setName(ROLE_ADMIN);
            roleRepository.save(role);
        }
    }

    private void initAdmin() {
        String adminEmail = env.getProperty("app.admin.email", "admin@bank.com");
        String adminPassword = env.getProperty("app.admin.password", "admin777");

        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName(ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("Admin role not found"));

            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setFullName("Bank Administrator");
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);
        }
    }
}