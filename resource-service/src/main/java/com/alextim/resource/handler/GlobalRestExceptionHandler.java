package com.alextim.resource.handler;

import com.alextim.resource.exception.NotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalRestExceptionHandler {
    private static final String ERROR_MESSAGE_KEY = "errorMessage";
    private static final String ERROR_CODE_KEY = "errorCode";

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ERROR_MESSAGE_KEY, ex.getMessage());
        body.put(ERROR_CODE_KEY, String.valueOf(NOT_FOUND.value()));

        return ResponseEntity
                .status(NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class
    })
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            Exception ex) {
        String message = "";
        if (ex instanceof HandlerMethodValidationException validationException) {
            Object value = validationException.getParameterValidationResults()
                    .get(0)
                    .getArgument();

            message = "Invalid value '%s' for ID. Must be a positive integer"
                    .formatted(value);
        } else if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            message = "Invalid value '%s' for ID. Must be a positive integer"
                    .formatted(mismatch.getValue());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ERROR_MESSAGE_KEY, message);
        body.put(ERROR_CODE_KEY, String.valueOf(BAD_REQUEST.value()));

        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ERROR_MESSAGE_KEY, ex.getMessage());
        body.put(ERROR_CODE_KEY, String.valueOf(BAD_REQUEST.value()));

        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

}
