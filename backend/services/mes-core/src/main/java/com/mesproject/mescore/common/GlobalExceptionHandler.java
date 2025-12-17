package com.mesproject.mescore.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse badCred(BadCredentialsException ex, HttpServletRequest req) {
        return new ErrorResponse(Instant.now(), 401, "UNAUTHORIZED", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse badReq(IllegalArgumentException ex, HttpServletRequest req) {
        return new ErrorResponse(Instant.now(), 400, "BAD_REQUEST", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse generic(Exception ex, HttpServletRequest req) {
        return new ErrorResponse(Instant.now(), 500, "INTERNAL_SERVER_ERROR", ex.getMessage(), req.getRequestURI());
    }
}
