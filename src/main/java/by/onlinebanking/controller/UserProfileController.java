package by.onlinebanking.controller;

import by.onlinebanking.dto.account.AccountDto;
import by.onlinebanking.dto.response.UserResponseDto;
import by.onlinebanking.dto.user.UpdateUserDto;
import by.onlinebanking.model.User;
import by.onlinebanking.service.AccountService;
import by.onlinebanking.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userService.findByEmail(email);
        return ResponseEntity.ok(new UserResponseDto(user));
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserResponseDto> updateMyProfile(@Valid @RequestBody UpdateUserDto userDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userService.findByEmail(email);
        return ResponseEntity.ok(userService.partialUpdateUser(user.getId(), userDto));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getMyAccounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userService.findByEmail(email);
        return ResponseEntity.ok(accountService.getAccountsByUserId(user.getId()));
    }
}
