package com.nexus.payment.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, WebRequest req) {
        logger.warn("Bad request: {} (Path: {})", ex.getMessage(), req.getDescription(false));
        return buildErrorResponse(ex.getMessage(), req, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, WebRequest req) {
        logger.error("Internal Server Error at {}: {}", req.getDescription(false), ex.getMessage(), ex);
        String userMessage = "Wystąpił wewnętrzny błąd serwera. Prosimy spróbować później.";

        return buildErrorResponse(userMessage, req, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, WebRequest req, HttpStatus status) {
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", req.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(body, status);
    }
}
