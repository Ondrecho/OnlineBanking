package by.onlinebanking.validation;

import by.onlinebanking.dto.RoleDto;
import by.onlinebanking.exception.ValidationException;
import by.onlinebanking.model.Role;
import by.onlinebanking.repository.RoleRepository;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RolesValidator {
    private final RoleRepository roleRepository;

    @Autowired
    public RolesValidator(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Set<Role> validateAndFindRoles(Set<RoleDto> roleDtos) {
        return roleDtos.stream()
                .map(roleDto -> {
                    Optional<Role> optionalRole = roleRepository.findByName(roleDto.getName());
                    if (optionalRole.isEmpty()) {
                        throw new ValidationException("Role not found")
                                .addDetail("roleName", roleDto.getName());
                    }
                    return optionalRole.get();
                })
                .collect(Collectors.toSet());
    }
}
