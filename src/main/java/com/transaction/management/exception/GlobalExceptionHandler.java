package com.transaction.management.exception;

import com.transaction.management.service.DeleteConfirmationException;
import com.transaction.management.service.DuplicateTransactionException;
import com.transaction.management.service.UpdateConfirmationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateTransaction(
            DuplicateTransactionException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "warning", exception.getMessage()
                ));
    }

    @ExceptionHandler(UpdateConfirmationException.class)
    public ResponseEntity<Map<String, String>> handleUpdateConfirmation(
            UpdateConfirmationException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "confirmation", exception.getMessage()
                ));
    }

    @ExceptionHandler(DeleteConfirmationException.class)
    public ResponseEntity<Map<String, String>> handleDeleteConfirmation(
            DeleteConfirmationException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "confirmation", exception.getMessage()
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }
}