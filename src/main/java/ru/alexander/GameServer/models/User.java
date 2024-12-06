package ru.alexander.GameServer.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
@Data
@Entity
@Table(name="users")
@Getter
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(unique=true, nullable=false)
    private long id;

    @Setter
    @Column(unique=true, nullable=false)
    private String username;

    @Setter
    @Column(nullable=false)
    private String name;

    @Setter
    @Column(nullable=false)
    private String surname;

    @Column(unique=true, nullable=false, updatable = false)
    private String email;

    @Column(nullable=false)
    private String password;

    public User(String username, String name, String surname, String email, String password) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
    }

    protected User() {}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

}
