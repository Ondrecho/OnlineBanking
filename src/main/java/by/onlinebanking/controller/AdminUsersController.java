package by.onlinebanking.controller;

import by.onlinebanking.dto.account.AccountDto;
import by.onlinebanking.dto.response.UserResponseDto;
import by.onlinebanking.dto.user.CreateUserDto;
import by.onlinebanking.dto.user.UpdateUserDto;
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
@RequestMapping("/api/admin/users")
public class AdminUsersController {
    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public AdminUsersController(UserService userService,
                               AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUserAsAdmin(@Valid @RequestBody CreateUserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<UserResponseDto>> createUsersBulk(
            @Validated @RequestBody List<@Valid CreateUserDto> userDtos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUsersBulk(userDtos));
    }

    @PostMapping("/{userId}/accounts")
    public ResponseEntity<AccountDto> createAccount(@PathVariable Long userId,
                                                    @NotNull @RequestParam Currency currency) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(userId, currency));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsers(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) List<String> roleNames
    ) {
        List<UserResponseDto> users = userService.getUsers(fullName, roleNames);

        if (users.isEmpty()) {
            throw new NotFoundException("No users found with the specified criteria")
                    .addDetail("fullName", fullName)
                    .addDetail("roleNames", roleNames);
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

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDto> partialUpdateUser(
            @PathVariable Long userId,
            @Validated(OnPatch.class) @RequestBody UpdateUserDto userDto
    ) {
        return ResponseEntity.ok(userService.partialUpdateUser(userId, userDto));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> fullUpdateUser(
            @PathVariable Long userId,
            @Validated(OnUpdate.class) @RequestBody UpdateUserDto userDto
    ) {
        return ResponseEntity.ok(userService.fullUpdateUser(userId, userDto));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUserAsAdmin(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}