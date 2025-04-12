package by.onlinebanking.controller;

import by.onlinebanking.dto.account.AccountDto;
import by.onlinebanking.dto.response.UserResponseDto;
import by.onlinebanking.dto.user.UpdateUserDto;
import by.onlinebanking.model.User;
import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.service.AccountService;
import by.onlinebanking.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserProfileController {
    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public UserProfileController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getMyProfile() {
        return ResponseEntity.ok(new UserResponseDto(userService.getUserFromAuthentication()));
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserResponseDto> updateMyProfile(@Valid @RequestBody UpdateUserDto userDto) {
        User currentUser = userService.getUserFromAuthentication();

        return ResponseEntity.ok(userService.partialUpdateUser(currentUser.getId(), userDto));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getMyAccounts() {
        User currentUser = userService.getUserFromAuthentication();

        return ResponseEntity.ok(accountService.getAccountsByUserId(currentUser.getId()));
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDto> createAccount(@NotNull @RequestParam Currency currency) {
        User currentUser = userService.getUserFromAuthentication();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(currentUser.getId(), currency));
    }
}
