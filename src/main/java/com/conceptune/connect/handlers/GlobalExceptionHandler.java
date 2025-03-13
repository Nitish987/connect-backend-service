package com.conceptune.connect.handlers;

import com.conceptune.connect.exceptions.GroupCreationException;
import com.conceptune.connect.exceptions.GroupMemberException;
import com.conceptune.connect.exceptions.MessagingException;
import com.conceptune.connect.security.exceptions.EncryptedTokenException;
import com.conceptune.connect.exceptions.NoDataFoundException;
import com.conceptune.connect.utils.Response;
import com.conceptune.connect.exceptions.AuthException;
import com.conceptune.connect.exceptions.SignalBundleException;
import com.google.cloud.storage.StorageException;
import io.jsonwebtoken.JwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.MethodNotAllowedException;

import javax.naming.AuthenticationException;
import java.util.List;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Response<?>> webExchangeBindExceptionHandler(WebExchangeBindException exception) {
        this.printErrorLog(exception);
        List<FieldError> fieldErrors = exception.getFieldErrors();
        if (fieldErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.error("Something went wrong."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.error(fieldErrors.get(0).getDefaultMessage()));
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<Response<?>> methodNotAllowedExceptionHandler(MethodNotAllowedException exception) {
        this.printErrorLog(exception);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(Response.error("Method not allowed."));
    }

    @ExceptionHandler({EncryptedTokenException.class, JwtException.class, AuthenticationException.class, AuthException.class})
    public ResponseEntity<Response<?>> unauthorizedExceptionHandler(Exception exception) {
        this.printErrorLog(exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Response.error("Invalid credentials or credentials not provided."));
    }

    @ExceptionHandler({DataAccessException.class, StorageException.class, NoDataFoundException.class})
    public ResponseEntity<Response<?>> dataAccessExceptionHandler(Exception exception) {
        this.printErrorLog(exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.error("Something went wrong while retrieving data."));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Response<?>> unsupportedOperationExceptionHandler(Exception exception) {
        this.printErrorLog(exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Response.error("Unsupported operation."));
    }

    @ExceptionHandler(SignalBundleException.class)
    public ResponseEntity<Response<?>> signalExceptionHandler(Exception exception) {
        this.printErrorLog(exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Response.error("Signal bundle invalid or unable to handle."));
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<Response<?>> messagingExceptionHandler(Exception exception) {
        this.printErrorLog(exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Response.error("Unable to process message."));
    }

    @ExceptionHandler({GroupCreationException.class, GroupMemberException.class})
    public ResponseEntity<Response<?>> groupsExceptionHandler(Exception exception) {
        this.printErrorLog(exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Response.error("Unable to process group."));
    }

    private void printErrorLog(Exception e) {
        log.error(e);
    }
}
