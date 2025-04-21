package by.onlinebanking.controller;

import by.onlinebanking.security.dto.request.ChangePasswordRequest;
import by.onlinebanking.security.dto.request.LoginRequest;
import by.onlinebanking.security.dto.request.RegisterRequest;
import by.onlinebanking.security.dto.response.JwtResponse;
import by.onlinebanking.security.service.AuthService;
import by.onlinebanking.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(
                request.getCurrentPassword(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );

        return ResponseEntity.ok().body(Map.of(
                "message", "Password changed successfully"
        ));
    }
}