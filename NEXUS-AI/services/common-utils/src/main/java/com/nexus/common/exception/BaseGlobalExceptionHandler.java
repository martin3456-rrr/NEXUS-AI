package com.nexus.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

public abstract class BaseGlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(BaseGlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest req) {
        logger.warn("Bad request: {} (Path: {})", ex.getMessage(), req.getDescription(false));
        return buildErrorResponse(ex.getMessage(), req, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest req) {
        logger.error("Internal Server Error at {}: {}", req.getDescription(false), ex.getMessage(), ex);
        String userMessage = "Wystąpił wewnętrzny błąd serwera. Prosimy spróbować później.";
        return buildErrorResponse(userMessage, req, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected ResponseEntity<ErrorResponse> buildErrorResponse(String message, WebRequest req, HttpStatus status) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(body, status);
    }
}