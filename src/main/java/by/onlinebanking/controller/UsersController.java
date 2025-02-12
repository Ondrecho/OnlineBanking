package by.onlinebanking.controller;

import by.onlinebanking.dto.UserDetailDto;
import by.onlinebanking.dto.UserDto;
import by.onlinebanking.service.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        if (!userDtoOptional.isPresent()) {
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

    @GetMapping("/detail/{userId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailDto> getDetailedUserById(@PathVariable Long userId) {
        Optional<UserDetailDto> userDetailDtoOptional = userService.getDetailedUserById(userId);
        if (!userDetailDtoOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDetailDtoOptional.get());
    }
}
