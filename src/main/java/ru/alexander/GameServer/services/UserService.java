package ru.alexander.GameServer.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexander.GameServer.models.GameSession;
import ru.alexander.GameServer.models.User;
import ru.alexander.GameServer.repositories.UserRepository;
import ru.alexander.GameServer.messages.RegisterDto;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void add(RegisterDto dto) {
        var password = passwordEncoder.encode(dto.getPassword());
        userRepository.save(new User(dto.getUsername(), dto.getName(), dto.getSurname(), dto.getEmail(), password));
    }
    public ResponseEntity<String> changeUsername(String username) {
        return executeForUser((user) -> {
            if (user.getUsername().equals(username))
                return new ResponseEntity<>("User already have this username!", HttpStatus.NOT_MODIFIED);
            if (existsByUsername(username))
                return new ResponseEntity<>("User with this username already exists!", HttpStatus.CONFLICT);
            user.setUsername(username);
            return new ResponseEntity<>("Renamed successfully!", HttpStatus.OK);
        });
    }
    public ResponseEntity<String> changePassword(String password) {
        return executeForUser((user) -> {
            user.setPassword(passwordEncoder.encode(password));
            return new ResponseEntity<>("Password changed successfully!", HttpStatus.OK);
        });
    }
    public ResponseEntity<String> delete() {
        return executeForUser((user) -> {
            userRepository.deleteById(user.getId());
            return new ResponseEntity<>("User deleted successfully!", HttpStatus.OK);
        });
    }
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    public boolean existsByEmail(String username) {
        return userRepository.existsByEmail(username);
    }

    public List<User> list() {
        return userRepository.findAll();
    }


    private ResponseEntity<String> executeForUser(Function<User, ResponseEntity<String>> action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated())
            return new ResponseEntity<>("User not authorized!", HttpStatus.UNAUTHORIZED);
        return action.apply((User) authentication.getPrincipal());
    }
}