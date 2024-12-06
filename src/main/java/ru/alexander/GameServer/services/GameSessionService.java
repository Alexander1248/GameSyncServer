package ru.alexander.GameServer.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexander.GameServer.models.GameSession;
import ru.alexander.GameServer.models.SessionUser;
import ru.alexander.GameServer.models.User;
import ru.alexander.GameServer.repositories.GameSessionRepository;
import ru.alexander.GameServer.repositories.SessionUserRepository;
import ru.alexander.GameServer.repositories.UserRepository;

import java.util.function.BiFunction;

@Service
@Transactional
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final SessionUserRepository sessionUserRepository;
    private final UserRepository userRepository;

    public GameSessionService(GameSessionRepository gameSessionRepository, SessionUserRepository sessionUserRepository, UserRepository userRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.sessionUserRepository = sessionUserRepository;
        this.userRepository = userRepository;
    }
    public ResponseEntity<String> create(String name) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated())
            return new ResponseEntity<>("User not authorized!", HttpStatus.UNAUTHORIZED);
        if (gameSessionRepository.existsByName(name))
            return new ResponseEntity<>("Server with this name already exists!", HttpStatus.CONFLICT);
        User user = (User) authentication.getPrincipal();
        GameSession session = new GameSession(name);
        gameSessionRepository.save(session);
        sessionUserRepository.save(new SessionUser(session, user, true));
        return new ResponseEntity<>("Server created!", HttpStatus.CREATED);
    }

    public ResponseEntity<?> connect(String name) {
        return executeForSession(name, (session, user) -> {
            if (sessionUserRepository.existsBySessionAndUser(session, user))
                return new ResponseEntity<>("User already connected!", HttpStatus.CONFLICT);
            sessionUserRepository.save(new SessionUser(session, user));
            return new ResponseEntity<>("Connected successfully!", HttpStatus.OK);
        });
    }

    public ResponseEntity<?> disconnect(String name) {
        return executeForSession(name, (session, user) -> {
            if (!sessionUserRepository.existsBySessionAndUser(session, user))
                return new ResponseEntity<>("User not connected!", HttpStatus.CONFLICT);
            sessionUserRepository.removeBySessionAndUser(session, user);
            return new ResponseEntity<>("Disconnected successfully!", HttpStatus.OK);
        });
    }
    public ResponseEntity<?> rename(String name, String newName) {
        return executeForSession(name, (session, user) -> {
            if (!sessionUserRepository.existsBySessionAndUser(session, user))
                return new ResponseEntity<>("User not connected!", HttpStatus.CONFLICT);
            if (!sessionUserRepository.findBySessionAndUser(session, user).isAdmin())
                return new ResponseEntity<>("User not admin!", HttpStatus.FORBIDDEN);
            session.setName(newName);
            return new ResponseEntity<>("Renamed successfully!", HttpStatus.OK);
        });
    }
    public ResponseEntity<?> close(String name) {
        return executeForSession(name, (session, user) -> {
            if (!sessionUserRepository.existsBySessionAndUser(session, user))
                return new ResponseEntity<>("User not connected!", HttpStatus.CONFLICT);
            if (!sessionUserRepository.findBySessionAndUser(session, user).isAdmin())
                return new ResponseEntity<>("User not admin!", HttpStatus.FORBIDDEN);
            gameSessionRepository.deleteById(session.getId());
            return new ResponseEntity<>("Renamed successfully!", HttpStatus.OK);
        });
    }
    public ResponseEntity<?> ban(String name, String banned) {
        return executeForSession(name, (session, user) -> {
            if (!sessionUserRepository.existsBySessionAndUser(session, user))
                return new ResponseEntity<>("User not connected!", HttpStatus.CONFLICT);
            if (!sessionUserRepository.findBySessionAndUser(session, user).isAdmin())
                return new ResponseEntity<>("User not admin!", HttpStatus.FORBIDDEN);
            if (!userRepository.existsByUsername(banned))
                return new ResponseEntity<>("User not found!", HttpStatus.CONFLICT);
            var bannedUser = userRepository.findByUsername(banned);
            if (!sessionUserRepository.existsBySessionAndUser(session, bannedUser))
                return new ResponseEntity<>("User for ban not connected!", HttpStatus.CONFLICT);
            sessionUserRepository.removeBySessionAndUser(session, bannedUser);
            return new ResponseEntity<>("Banned successfully!", HttpStatus.OK);
        });
    }

    private ResponseEntity<?> executeForSession(String name, BiFunction<GameSession, User, ResponseEntity<?>> action) {
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
    public boolean isConnected(String name, String user) {
        if (gameSessionRepository.existsByName(name))
            return sessionUserRepository.findBySessionAndUser(
                    gameSessionRepository.findByName(name),
                    userRepository.findByUsername(user)
            ).isAdmin();
        return false;
    }
    public boolean isConnected(String name, User user) {
        if (gameSessionRepository.existsByName(name))
            return sessionUserRepository.findBySessionAndUser(
                    gameSessionRepository.findByName(name),
                    user
            ).isAdmin();
        return false;
    }
    public boolean isAdmin(String name, String user) {
        if (gameSessionRepository.existsByName(name) && userRepository.existsByUsername(user))
            return sessionUserRepository.findBySessionAndUser(
                    gameSessionRepository.findByName(name),
                    userRepository.findByUsername(user)
            ).isAdmin();
        return false;
    }
    public ResponseEntity<?> changeAdmin(String name, String username, boolean admin) {
        return executeForSession(name, (session, user) -> {
            if (!sessionUserRepository.existsBySessionAndUser(session, user))
                return new ResponseEntity<>("User not connected!", HttpStatus.CONFLICT);
            if (!sessionUserRepository.findBySessionAndUser(session, user).isAdmin())
                return new ResponseEntity<>("User not admin!", HttpStatus.FORBIDDEN);
            if (!userRepository.existsByUsername(username))
                return new ResponseEntity<>("User not found!", HttpStatus.CONFLICT);
            var opedUser = userRepository.findByUsername(username);
            if (!sessionUserRepository.existsBySessionAndUser(session, opedUser))
                return new ResponseEntity<>("User for op not connected!", HttpStatus.CONFLICT);
            sessionUserRepository.findBySessionAndUser(
                    gameSessionRepository.findByName(name),
                    opedUser
            ).setAdmin(admin);
            return new ResponseEntity<>("Oped successfully!", HttpStatus.OK);
        });
    }

    public ResponseEntity<?> users(String name) {
        return executeForSession(name, (session, user) -> {
            if (!sessionUserRepository.existsBySessionAndUser(session, user))
                return new ResponseEntity<>("User not connected!", HttpStatus.CONFLICT);
            return new ResponseEntity<>(sessionUserRepository.findAllBySession(session), HttpStatus.OK);
        });
    }
}
