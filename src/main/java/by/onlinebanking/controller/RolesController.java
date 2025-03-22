package by.onlinebanking.controller;

import by.onlinebanking.dto.RoleDto;
import by.onlinebanking.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolesController {
    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleDto roleDto) {
        RoleDto createdRoleDto = roleService.createRole(roleDto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoleDto);
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long roleId,
                                              @Valid @RequestBody RoleDto roleDto) {
        return ResponseEntity.ok(roleService.updateRole(roleId, roleDto));
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }
}

