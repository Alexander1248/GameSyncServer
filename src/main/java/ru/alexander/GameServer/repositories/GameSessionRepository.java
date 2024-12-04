package ru.alexander.GameServer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexander.GameServer.models.GameSession;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    GameSession findById(long id);
    GameSession findByName(String name);

    boolean existsById(long id);
    boolean existsByName(String name);
}
