package ru.alexander.GameServer.messages;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class RegisterDto {
    private String username;
    private String name;
    private String surname;
    private String email;
    private String password;
}