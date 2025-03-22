package by.onlinebanking.controller;

import by.onlinebanking.dto.CreateUserDto;
import by.onlinebanking.dto.UpdateUserDto;
import by.onlinebanking.dto.UserResponseDto;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.service.UserService;
import by.onlinebanking.validation.annotations.IbanFormat;
import by.onlinebanking.validation.interfaces.OnPatch;
import by.onlinebanking.validation.interfaces.OnUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Validated
public class UsersController {
    private final UserService userService;

    @Autowired
    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
        UserResponseDto response = userService.getUserById(userId);
        if (response == null) {
            throw new NotFoundException("User not found")
                    .addDetail("userId", userId);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-name")
    public ResponseEntity<List<UserResponseDto>> getUserByName(@RequestParam @NotBlank String fullName) {
        List<UserResponseDto> responseList = userService.getUsersByName(fullName);
        if (responseList.isEmpty()) {
            throw new NotFoundException("No users found with name: " + fullName)
                    .addDetail("fullName", fullName);
        }
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/by-role")
    public ResponseEntity<List<UserResponseDto>> getUserByRole(@RequestParam @NotBlank String roleName) {
        List<UserResponseDto> responseList = userService.getUsersByRole(roleName);
        if (responseList.isEmpty()) {
            throw new NotFoundException("No users found with role: " + roleName)
                    .addDetail("roleName", roleName);
        }
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/by-iban/{iban}")
    public ResponseEntity<UserResponseDto> getUserByIban(@PathVariable @IbanFormat String iban) {
        UserResponseDto response = userService.getUserByIban(iban);
        if (response == null) {
            throw new NotFoundException("No users found with IBAN: " + iban);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserDto userDto) {
        UserResponseDto createdUserDto = userService.createUser(userDto);
        return ResponseEntity.status(201).body(createdUserDto);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> fullUpdateUser(
            @PathVariable Long userId,
            @Validated(OnUpdate.class) @RequestBody UpdateUserDto userDto
    ) {
        UserResponseDto updatedUserDto = userService.fullUpdateUser(userId, userDto);
        return ResponseEntity.ok(updatedUserDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDto> partialUpdateUser(
            @PathVariable Long userId,
            @Validated(OnPatch.class) @RequestBody UpdateUserDto userDto
    ) {
        UserResponseDto updatedUserDto = userService.partialUpdateUser(userId, userDto);
        return ResponseEntity.ok(updatedUserDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
