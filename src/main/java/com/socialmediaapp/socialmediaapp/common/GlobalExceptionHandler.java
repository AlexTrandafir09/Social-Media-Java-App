package com.socialmediaapp.socialmediaapp.common;

import com.socialmediaapp.socialmediaapp.content.CommentNotFoundException;
import com.socialmediaapp.socialmediaapp.content.DuplicateLikeException;
import com.socialmediaapp.socialmediaapp.content.LikeNotFoundException;
import com.socialmediaapp.socialmediaapp.content.PostImageNotFoundException;
import com.socialmediaapp.socialmediaapp.content.PostMustHaveImageException;
import com.socialmediaapp.socialmediaapp.content.PostNotFoundException;
import com.socialmediaapp.socialmediaapp.notification.NotificationNotFoundException;
import com.socialmediaapp.socialmediaapp.user.exception.DuplicateEmailException;
import com.socialmediaapp.socialmediaapp.user.exception.DuplicateFollowException;
import com.socialmediaapp.socialmediaapp.user.exception.DuplicateUsernameException;
import com.socialmediaapp.socialmediaapp.user.exception.FollowNotFoundException;
import com.socialmediaapp.socialmediaapp.user.exception.InvalidCurrentPasswordException;
import com.socialmediaapp.socialmediaapp.user.exception.SelfFollowException;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.exception.UserPreferenceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            UserNotFoundException.class,
            FollowNotFoundException.class,
            UserPreferenceNotFoundException.class,
            PostNotFoundException.class,
            CommentNotFoundException.class,
            LikeNotFoundException.class,
            PostImageNotFoundException.class,
            NotificationNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        log.debug("Not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            DuplicateUsernameException.class,
            DuplicateEmailException.class,
            DuplicateFollowException.class,
            DuplicateLikeException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        log.debug("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            InvalidCurrentPasswordException.class,
            SelfFollowException.class,
            PostMustHaveImageException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        log.debug("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    // Placeholder until Phase 03's cascade/deletion-order decision is implemented per entity.
    @ExceptionHandler({DataIntegrityViolationException.class, InvalidDataAccessApiUsageException.class})
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(RuntimeException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("This record can't be modified because other records still reference it"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        log.debug("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Something went wrong"));
    }
}
