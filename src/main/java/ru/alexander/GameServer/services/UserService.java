package ru.alexander.GameServer.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexander.GameServer.models.User;
import ru.alexander.GameServer.repositories.UserRepository;
import ru.alexander.GameServer.messages.RegisterDto;

import java.util.List;

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
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    public boolean existsByEmail(String username) {
        return userRepository.existsByEmail(username);
    }

    public List<User> list() {
        return userRepository.findAll();
    }
}