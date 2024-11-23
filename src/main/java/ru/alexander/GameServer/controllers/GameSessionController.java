package ru.alexander.GameServer.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.alexander.GameServer.messages.GameMessage;
import ru.alexander.GameServer.models.User;
import ru.alexander.GameServer.services.GameSessionService;

@Controller
@RequestMapping("sessions")
public class GameSessionController {

    private final GameSessionService service;
    private final SimpMessagingTemplate template;

    public GameSessionController(GameSessionService service, SimpMessagingTemplate template) {
        this.service = service;
        this.template = template;
    }

    @PostMapping("/send/{server}")
    public ResponseEntity<String> send(
            @PathVariable String server,
            @RequestParam String type,
            @RequestParam(required = false) String user,
            @RequestBody byte[] data) {
        if (!service.exists(server))
            return new ResponseEntity<>("Server not exists!", HttpStatus.NOT_FOUND);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated())
            return new ResponseEntity<>("User not authorized!", HttpStatus.UNAUTHORIZED);
        User source = (User) authentication.getPrincipal();
        if (!service.isConnected(server, source))
            return new ResponseEntity<>("User not connected to server!", HttpStatus.PRECONDITION_REQUIRED);

        var msg = new GameMessage(source.getUsername(), type, data);
        if (user != null)
            template.convertAndSendToUser(user, "/sessions/" + server, msg);
        else
            template.convertAndSend("/sessions/" + server, msg);
        return new ResponseEntity<>("Message sent!", HttpStatus.OK);
    }

    @PostMapping("/create/{server}")
    public ResponseEntity<String> create(@PathVariable String server) {
        return service.create(server);
    }
    @PostMapping("/connect/{server}")
    public ResponseEntity<String> connect(@PathVariable String server) {
        return service.connect(server);
    }
    @PostMapping("/disconnect/{server}")
    public ResponseEntity<String> disconnect(@PathVariable String server) {
        return service.disconnect(server);
    }
}
