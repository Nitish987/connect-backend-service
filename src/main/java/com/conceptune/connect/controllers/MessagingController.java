package com.conceptune.connect.controllers;

import com.conceptune.connect.broker.dto.MultiMessage;
import com.conceptune.connect.dto.request.NewMessageToken;
import com.conceptune.connect.utils.Regex;
import com.conceptune.connect.utils.Response;
import com.conceptune.connect.broker.dto.Message;
import com.conceptune.connect.services.MessagingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messaging")
public class MessagingController {

    @Autowired
    private MessagingService messagingService;

    @PostMapping("/publish")
    public ResponseEntity<Response<?>> publishMessage(@RequestBody @Valid Message message) throws Exception {
        Response<?> response = messagingService.publishMessage(message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/multi-publish")
    public ResponseEntity<Response<?>> publishMultiMessage(@RequestBody @Valid MultiMessage multiMessage) throws Exception {
        Response<?> response = messagingService.publishMultiMessage(multiMessage);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/token")
    public ResponseEntity<Response<?>> updateMessageToken(Authentication auth, @RequestBody @Valid NewMessageToken messageToken) throws Exception {
        Response<?> response = messagingService.updateMessageToken(auth.getPrincipal().toString(), messageToken);
        return ResponseEntity.ok(response);
    }
}
