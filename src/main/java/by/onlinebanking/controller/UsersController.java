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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.ResponseStatus;
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
    public ResponseEntity<Page<UserResponseDto>> getUsers(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) List<String> roleNames,
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable
    ) {
        Page<UserResponseDto> users = userService.getUsers(fullName, roleNames, pageable);

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

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<UserResponseDto>> createUsersBulk(
            @Validated @RequestBody List<@Valid CreateUserDto> userDtos) {
        List<UserResponseDto> createdUsers = userService.createUsersBulk(userDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUsers);
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
