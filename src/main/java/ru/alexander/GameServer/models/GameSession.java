package ru.alexander.GameServer.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="sessions")
@Getter
public class GameSession {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(unique=true, nullable=false)
    private long id;

    @Setter
    @Column(unique=true, nullable=false)
    private String name;

    @OneToMany
    private List<User> users;

    public GameSession(String name) {
        this.name = name;
        users = new ArrayList<>();
    }

    public GameSession() {}
}
