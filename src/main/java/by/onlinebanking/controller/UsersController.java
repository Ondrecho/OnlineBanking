package by.onlinebanking.controller;

import by.onlinebanking.dto.UserCreateUpdateDto;
import by.onlinebanking.dto.UserDto;
import by.onlinebanking.service.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    private final UserService userService;

    @Autowired
    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> userDtoList = userService.getAllUsers();
        return ResponseEntity.ok(userDtoList);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        Optional<UserDto> userDtoOptional = userService.getUserById(userId);
        if (userDtoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDtoOptional.get());
    }

    @GetMapping("/by-name")
    public ResponseEntity<List<UserDto>> getUserByName(@RequestParam String fullName) {
        List<UserDto> userDtoList = userService.getUserByName(fullName);
        if (userDtoList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDtoList);
    }

    @PostMapping("/")
    public ResponseEntity<UserDto> createUser(@Validated @RequestBody UserCreateUpdateDto user) {
        UserDto createdUserDto = userService.createUser(user);
        return ResponseEntity.status(201).body(createdUserDto);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @Validated @RequestBody UserCreateUpdateDto userCreateUpdateDto) {
        Optional<UserDto> updatedUserDtoOptional = userService.updateUser(userId, userCreateUpdateDto);
        if (updatedUserDtoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUserDtoOptional.get());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        boolean deleted = userService.deleteUser(userId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("User deleted successfully");
    }
}
