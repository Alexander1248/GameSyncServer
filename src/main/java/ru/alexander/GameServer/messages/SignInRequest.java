package ru.alexander.GameServer.messages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class SignInRequest {
    private String username;
    private String password;
}