package ru.alexander.GameServer.controllers;

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
    public List<User> GetAllUsers() {
        return userService.list();
    }
}
