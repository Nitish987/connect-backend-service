package com.conceptune.connect.controllers;

import com.conceptune.connect.utils.Regex;
import com.conceptune.connect.utils.Response;
import com.conceptune.connect.dto.request.SignalPreKeyBundle;
import com.conceptune.connect.services.SignalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/signal")
public class SignalController {

    @Autowired
    private SignalService signalService;

    @GetMapping("/device-id")
    public ResponseEntity<Response<Long>> retrieveDeviceId(@RequestParam @Valid @Pattern(regexp = Regex.NO_HTML) String userId) {
        Long deviceId = signalService.retrieveDeviceId(userId);
        return ResponseEntity.ok(Response.success("Device Id", deviceId));
    }

    @GetMapping("/pre-key-bundle")
    public ResponseEntity<Response<?>> retrieveSignalPreKeyBundle(@RequestParam @Valid @Pattern(regexp = Regex.NO_HTML) String userId) {
        Response<?> response = signalService.retrieveSignalPreKeyBundle(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-device")
    public ResponseEntity<Response<?>> registerSignalDevice(Authentication auth, @RequestBody @Valid SignalPreKeyBundle signalPreKeyBundle) throws Exception {
        Response<?> response = signalService.registerSignalDevice(auth.getPrincipal().toString(), signalPreKeyBundle);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-device")
    public ResponseEntity<Response<?>> refreshSignalDevice(Authentication auth, @RequestBody @Valid SignalPreKeyBundle signalPreKeyBundle) throws Exception {
        Response<?> response = signalService.refreshSignalDevice(auth.getPrincipal().toString(), signalPreKeyBundle);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/append-pre-keys")
    public ResponseEntity<Response<?>> appendPreKeys(Authentication auth, @RequestBody @Valid SignalPreKeyBundle signalPreKeyBundle) throws Exception {
        Response<?> response = signalService.appendPreKeys(auth.getPrincipal().toString(), signalPreKeyBundle);
        return ResponseEntity.ok(response);
    }
}
