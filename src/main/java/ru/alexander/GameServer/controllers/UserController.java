package ru.alexander.GameServer.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alexander.GameServer.models.User;
import ru.alexander.GameServer.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/users/")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("list")
    public List<User> getAllUsers() {
        return userService.list();
    }

    @PatchMapping("change/username")
    public ResponseEntity<String> changeUsername(@RequestParam String username) {
        return userService.changeUsername(username);
    }
    @PatchMapping("change/password")
    public ResponseEntity<String> changePassword(@RequestParam String password) {
        return userService.changePassword(password);
    }
    @DeleteMapping("delete")
    public ResponseEntity<String> delete() {
        return userService.delete();
    }



}
