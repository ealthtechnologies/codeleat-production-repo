package com.ealth.codeleat.exception_handler;

import com.ealth.codeleat.exceptions.DuplicateEmailException;
import com.ealth.codeleat.exceptions.InvalidOperationException;
import com.ealth.codeleat.exceptions.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(
            String message,
            int status,
            String error,
            Instant timestamp
    ) {}

    //WARN - expected, user triggered a DB constraint we didn't catch earlier
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.warn("Data integrity violation | method:{} | path:{} | cause:{}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMostSpecificCause().getMessage()); // log real cause internally

        ErrorResponse response = new ErrorResponse(
                "Request could not be completed due to a data conflict.", // vague to client
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    //WARN - expected, business rule violation you threw intentionally
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(
            InvalidOperationException ex,
            HttpServletRequest request) {

        log.warn("Invalid operation | method:{} | path:{} | reason:{}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                ex.getMessage(), // safe - you wrote this message yourself
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //WARN - expected, duplicate registration attempt
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex,
            HttpServletRequest request) {

        log.warn("Duplicate email attempt | method:{} | path:{}",
                request.getMethod(),
                request.getRequestURI());

        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    //WARN - expected, rate limiting working as intended
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(
            TooManyRequestsException ex,
            HttpServletRequest request) {

        log.warn("Rate limit exceeded | method:{} | path:{}",
                request.getMethod(),
                request.getRequestURI());

        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    //ERROR - catch all, something genuinely unexpected broke
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception | method:{} | path:{} | exception:{} | message:{}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex);

        ErrorResponse response = new ErrorResponse(
                "Something went wrong. Please try again.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

