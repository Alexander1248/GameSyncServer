package ru.alexander.GameServer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import ru.alexander.GameServer.models.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsById(long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    List<User> findAll();

    User findById(long id);
    User findByUsername(String username);
    User findByEmail(String email);

    User removeById(long id);
    User removeByUsername(String username);
    User removeByEmail(String email);
}
