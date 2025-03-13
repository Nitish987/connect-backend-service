package com.conceptune.connect.controllers;

import com.conceptune.connect.utils.Response;
import com.conceptune.connect.dto.request.NewAccount;
import com.conceptune.connect.dto.request.SignIn;
import com.conceptune.connect.dto.request.OtpPayload;
import com.conceptune.connect.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<Response<?>> signIn(@RequestBody @Valid SignIn signIn) throws Exception {
        String token = authService.signIn(signIn);
        return ResponseEntity.ok(Response.token(token));
    }

    @PostMapping("/verify")
    public ResponseEntity<Response<?>> verify(@RequestHeader("cot") String token, @RequestBody @Valid OtpPayload verification) throws Exception {
        Response<?> response = authService.verify(token, verification);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Response<?>> refresh(@RequestHeader("rt") String refreshToken, @RequestHeader("lst") String loginStateToken) throws Exception {
        Response<?> response = authService.refresh(refreshToken, loginStateToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/new-user-sign-in")
    public ResponseEntity<Response<?>> createNewUserAndSignIn(@RequestHeader("cnut") String token, @RequestBody @Valid NewAccount account) throws Exception {
        Response<?> response = authService.createNewUserAndSignIn(token, account);
        return ResponseEntity.ok(response);
    }
}
