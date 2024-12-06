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

    @PostMapping("/send")
    public ResponseEntity<String> send(
            @RequestParam String server,
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
            template.convertAndSendToUser(user, "/service/" + server, msg);
        else
            template.convertAndSend("/service/" + server, msg);
        return new ResponseEntity<>("Message sent!", HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestParam String server) {
        return service.create(server);
    }
    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestParam String server) {
        return service.connect(server);
    }
    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(@RequestParam String server) {
        return service.disconnect(server);
    }
    @PatchMapping("/ban")
    public ResponseEntity<?> users(@RequestParam String server, @RequestParam String user) {
        return service.ban(server, user);
    }
    @PatchMapping("/op")
    public ResponseEntity<?> addAdmin(@RequestParam String server,
                                      @RequestParam String user,
                                      @RequestParam(defaultValue = "true") boolean admin) {
        return service.changeAdmin(server, user, admin);
    }

    @PatchMapping("/rename")
    public ResponseEntity<?> rename(@RequestParam String server, @RequestParam String name) {
        return service.rename(server, name);
    }
    @PatchMapping("/users")
    public ResponseEntity<?> users(@RequestParam String server) {
        return service.users(server);
    }

    @DeleteMapping("/close")
    public ResponseEntity<?> close(@RequestParam String server) {
        return service.close(server);
    }
}
