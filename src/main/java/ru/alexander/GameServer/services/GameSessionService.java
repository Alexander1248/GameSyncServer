package ru.alexander.GameServer.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexander.GameServer.models.GameSession;
import ru.alexander.GameServer.models.User;
import ru.alexander.GameServer.repositories.GameSessionRepository;

import java.util.function.BiFunction;

@Service
@Transactional
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;

    public GameSessionService(GameSessionRepository gameSessionRepository) {
        this.gameSessionRepository = gameSessionRepository;
    }
    public ResponseEntity<String> create(String name) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated())
            return new ResponseEntity<>("User not authorized!", HttpStatus.UNAUTHORIZED);
        if (gameSessionRepository.existsByName(name))
            return new ResponseEntity<>("Server with this name already exists!", HttpStatus.CONFLICT);
        gameSessionRepository.save(new GameSession(name));
        return new ResponseEntity<>("Server created!", HttpStatus.CREATED);
    }

    public ResponseEntity<String> connect(String name) {
        return executeForSession(name, (session, user) -> {
            if (session.getUsers().contains(user))
                return new ResponseEntity<>("User already connected!", HttpStatus.CONFLICT);
            session.getUsers().add(user);
            return new ResponseEntity<>("Connected successfully!", HttpStatus.OK);
        });
    }

    public ResponseEntity<String> disconnect(String name) {
        return executeForSession(name, (session, user) -> {
            if (!session.getUsers().contains(user))
                return new ResponseEntity<>("User not connected!", HttpStatus.CONFLICT);
            session.getUsers().remove(user);
            return new ResponseEntity<>("Disconnected successfully!", HttpStatus.OK);
        });
    }

    private ResponseEntity<String> executeForSession(String name, BiFunction<GameSession, User, ResponseEntity<String>> action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated())
            return new ResponseEntity<>("User not authorized!", HttpStatus.UNAUTHORIZED);
        if (!gameSessionRepository.existsByName(name))
            return new ResponseEntity<>("Server not found!", HttpStatus.NOT_FOUND);

        return action.apply(gameSessionRepository.findByName(name), (User) authentication.getPrincipal());
    }
    public boolean exists(String name) {
        return gameSessionRepository.existsByName(name);
    }
    public boolean isConnected(String name, User user) {
        if (gameSessionRepository.existsByName(name))
            return gameSessionRepository.findByName(name).getUsers().contains(user);
        return false;
    }
}
