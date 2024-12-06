package ru.alexander.GameServer.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;


@Data
@Entity
@Table(name="sessions_users")
@Getter
@NoArgsConstructor
public class SessionUser {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(unique=true, nullable=false)
    private long id;

    @ManyToOne
    private GameSession session;

    @OneToOne(orphanRemoval = true)
    private User user;

    @Setter
    @Column(unique=true, nullable=false)
    private boolean admin;

    public SessionUser(GameSession session, User user) {
        this.session = session;
        this.user = user;
        this.admin = false;
    }
    public SessionUser(GameSession session, User user, boolean isAdmin) {
        this.session = session;
        this.user = user;
        this.admin = isAdmin;
    }
}
