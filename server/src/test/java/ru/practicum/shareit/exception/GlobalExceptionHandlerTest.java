package ru.practicum.shareit.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для класса {@link GlobalExceptionHandler}.
 * Тестирует обработку различных типов исключений и корректность формирования ответов.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    /**
     * Инициализация обработчика исключений перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    /**
     * Тест обработки исключения {@link NotFoundException}.
     * Проверяет, что обработчик возвращает статус 404 NOT_FOUND
     * и корректное сообщение об ошибке.
     */
    @Test
    void handleNotFoundException_shouldReturnNotFoundResponse() {
        NotFoundException exception = new NotFoundException("Не найдено");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Не найдено", response.getBody().getError());
    }

    /**
     * Тест обработки исключения {@link ValidationException}.
     * Проверяет, что обработчик возвращает статус 400 BAD_REQUEST
     * и корректное сообщение об ошибке валидации.
     */
    @Test
    void handleValidationException_shouldReturnBadRequestResponse() {
        ValidationException exception = new ValidationException("Ошибка валидации");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ошибка валидации", response.getBody().getError());
    }

    /**
     * Тест обработки исключения {@link ConflictException}.
     * Проверяет, что обработчик возвращает статус 409 CONFLICT
     * и корректное сообщение о конфликте.
     */
    @Test
    void handleConflictException_shouldReturnConflictResponse() {
        ConflictException exception = new ConflictException("Конфликт");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflictException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Конфликт", response.getBody().getError());
    }

    /**
     * Тест обработки исключения {@link AccessDeniedException}.
     * Проверяет, что обработчик возвращает статус 403 FORBIDDEN
     * и корректное сообщение о запрете доступа.
     */
    @Test
    void handleAccessDeniedException_shouldReturnForbiddenResponse() {
        AccessDeniedException exception = new AccessDeniedException("Доступ запрещен");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Доступ запрещен", response.getBody().getError());
    }

    /**
     * Тест обработки исключения {@link IllegalArgumentException}.
     * Проверяет, что обработчик возвращает статус 400 BAD_REQUEST
     * и корректное сообщение о неверном аргументе.
     */
    @Test
    void handleIllegalArgumentException_shouldReturnBadRequestResponse() {
        IllegalArgumentException exception = new IllegalArgumentException("Неверный аргумент");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Неверный аргумент", response.getBody().getError());
    }
}



