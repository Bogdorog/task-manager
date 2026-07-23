package com.sergeev.taskmanager.exception;

import com.sergeev.taskmanager.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String DUPLICATE_KEY_SQL_STATE = "23505";
    private static final String FOREIGN_KEY_VIOLATION_SQL_STATE = "23503";
    private static final String NOT_NULL_VIOLATION_SQL_STATE = "23502";
    private static final String CHECK_VIOLATION_SQL_STATE = "23514";
    private static final Pattern DUPLICATE_KEY_PATTERN = Pattern.compile("\\(([^)]+)\\)=\\(([^)]+)\\)");

    // Ошибки базы данных
    @ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(SQLException ex, HttpServletRequest request) {
        String sqlState = ex.getSQLState();
        HttpStatus status;
        ErrorResponse errorResponse;
        switch (sqlState) {
            case DUPLICATE_KEY_SQL_STATE -> {
                String message = extractDuplicateFieldMessage(ex);
                status = HttpStatus.CONFLICT;
                errorResponse = new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                );
            }
            case FOREIGN_KEY_VIOLATION_SQL_STATE -> {
                status = HttpStatus.CONFLICT;
                errorResponse = new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        "Нарушение ссылочной целостности",
                        request.getRequestURI()
                );
            }
            case NOT_NULL_VIOLATION_SQL_STATE, CHECK_VIOLATION_SQL_STATE -> {
                status = HttpStatus.BAD_REQUEST;
                errorResponse = new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Ошибка валидации данных",
                        request.getRequestURI()
                );
            }
            default -> {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                errorResponse = new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "Ошибка базы данных",
                        request.getRequestURI()
                );
            }
        }
        return new ResponseEntity<>(errorResponse, status);
    }

    // Обработка ошибок валидации (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                errorMessage,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    // Обработка нарушения бизнес-логики
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleExceptions(BusinessRuleException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Обработка других общих исключений
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleInternalError(Exception ex,
                                                             HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Произошла непредвиденная ошибка. " + ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Обработка исключения на случай падения SSE
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public void handleAsyncRequestTimeout(AsyncRequestTimeoutException ex, HttpServletRequest request) {
        log.debug("Async request timeout: {}", request.getRequestURI());
        // намеренно ничего не возвращаем
    }

    private String extractDuplicateFieldMessage(Throwable ex) {
        String fullMessage = ex.getMessage();
        if (fullMessage == null) return "Дублирующееся значение уникального поля";

        Matcher matcher = DUPLICATE_KEY_PATTERN.matcher(fullMessage);
        if (matcher.find()) {
            String field = matcher.group(1);
            String value = matcher.group(2);
            return String.format("Значение '%s' для поля '%s' уже существует", value, field);
        }
        return "Дублирующееся значение уникального поля";
    }
}
