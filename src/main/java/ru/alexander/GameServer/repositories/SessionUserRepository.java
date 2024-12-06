package ru.alexander.GameServer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexander.GameServer.models.GameSession;
import ru.alexander.GameServer.models.SessionUser;
import ru.alexander.GameServer.models.User;

import java.util.List;

public interface SessionUserRepository extends JpaRepository<SessionUser, User> {
    List<SessionUser> findAllBySession(GameSession session);
    List<SessionUser> findAllBySessionAndAdmin(GameSession session, boolean admin);
    SessionUser findBySessionAndUser(GameSession session, User user);

    void removeBySessionAndUser(GameSession session, User user);

    boolean existsBySessionAndUser(GameSession session, User user);

}
