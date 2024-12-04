package ru.alexander.GameServer.configs.auth;

import jakarta.security.auth.message.AuthException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompEncoder;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.alexander.GameServer.models.User;
import ru.alexander.GameServer.services.GameSessionService;

import static org.springframework.messaging.simp.stomp.DefaultStompSession.EMPTY_PAYLOAD;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    public final GameSessionService service;
    public final HeaderAuthProvider provider;

    public AuthChannelInterceptor(GameSessionService service, HeaderAuthProvider provider) {
        this.service = service;
        this.provider = provider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        System.out.println(accessor);
        if (accessor.getCommand() != StompCommand.CONNECT
                && accessor.getCommand() != StompCommand.SUBSCRIBE
                && accessor.getCommand() != StompCommand.SEND)
            return message;

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null)
            provider.auth(authHeader);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            throw new MessagingException(message, "User not authenticated!");

        if (accessor.getCommand() != StompCommand.SUBSCRIBE) return message;
        String server = accessor.getDestination();
        if (!server.startsWith("/service/")) return message;
        server = server.substring("/service/".length());

        if (!service.exists(server))
            throw new MessagingException(message, "Server not found!");
        if (auth.getPrincipal() instanceof User user
                && service.isConnected(server, user))
            return message;

        throw new MessagingException(message, "User not connected to server!");

    }
}
