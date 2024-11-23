package ru.alexander.GameServer.messages;

public record GameMessage(String source, String type, Object message) {
}
