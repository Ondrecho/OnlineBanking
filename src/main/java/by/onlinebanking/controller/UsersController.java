package by.onlinebanking.controller;

import by.onlinebanking.dto.AccountDto;
import by.onlinebanking.dto.CreateUserDto;
import by.onlinebanking.dto.UpdateUserDto;
import by.onlinebanking.dto.UserResponseDto;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.service.AccountService;
import by.onlinebanking.service.UserService;
import by.onlinebanking.validation.interfaces.OnPatch;
import by.onlinebanking.validation.interfaces.OnUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private final AccountService accountService;

    @Autowired
    public UsersController(UserService userService,
                           AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsers(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String roleName
    ) {
        List<UserResponseDto> users = userService.getUsers(fullName, roleName);

        if (users.isEmpty()) {
            throw new NotFoundException("No users found with the specified criteria");
        }

        return ResponseEntity.ok(users);
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

    @GetMapping("/{userId}/accounts")
    public ResponseEntity<List<AccountDto>> getUserAccounts(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    @PostMapping("/{userId}/accounts")
    public ResponseEntity<AccountDto> createAccount(@PathVariable Long userId,
                                                    @NotNull @RequestParam Currency currency) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(userId, currency));
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
