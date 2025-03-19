package by.onlinebanking.controller;

import by.onlinebanking.dto.UserDto;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.service.UserService;
import by.onlinebanking.service.validation.annotations.IbanFormat;
import by.onlinebanking.service.validation.interfaces.OnCreate;
import by.onlinebanking.service.validation.interfaces.OnPatch;
import by.onlinebanking.service.validation.interfaces.OnUpdate;
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
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> userDtoList = userService.getAllUsers();
        return ResponseEntity.ok(userDtoList);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        UserDto userDto = userService.getUserById(userId);
        if (userDto == null) {
            throw new NotFoundException("User not found")
                    .addDetail("userId", userId);
        }
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/by-name")
    public ResponseEntity<List<UserDto>> getUserByName(@RequestParam @NotBlank String fullName) {
        List<UserDto> userDtoList = userService.getUsersByName(fullName);
        if (userDtoList.isEmpty()) {
            throw new NotFoundException("No users found with name: " + fullName)
                    .addDetail("fullName", fullName);
        }
        return ResponseEntity.ok(userDtoList);
    }

    @GetMapping("/by-role")
    public ResponseEntity<List<UserDto>> getUserByRole(@RequestParam @NotBlank String roleName) {
        List<UserDto> users = userService.getUsersByRole(roleName);
        if (users.isEmpty()) {
            throw new NotFoundException("No users found with role: " + roleName)
                    .addDetail("roleName", roleName);
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-iban/{iban}")
    public ResponseEntity<UserDto> getUserByIban(@PathVariable @IbanFormat String iban) {
        UserDto userDto = userService.getUserByIban(iban);
        if (userDto == null) {
            throw new NotFoundException("No users found with IBAN: " + iban);
        }
        return ResponseEntity.ok(userDto);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Validated(OnCreate.class) @RequestBody UserDto userDto) {
        UserDto createdUserDto = userService.createUser(userDto);
        return ResponseEntity.status(201).body(createdUserDto);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> fullUpdateUser(@PathVariable Long userId,
                                                  @Validated(OnUpdate.class) @RequestBody UserDto userDto) {
        if (userDto.getPassword() == null || userDto.getEmail() == null) {
            throw new IllegalArgumentException("Missing required fields");
        }

        UserDto updatedUser = userService.fullUpdateUser(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> partialUpdateUser(@PathVariable Long userId,
                                                     @Validated(OnPatch.class) @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.partialUpdateUser(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
