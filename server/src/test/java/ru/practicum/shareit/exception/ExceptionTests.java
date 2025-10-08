package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для классов исключений.
 */
class ExceptionTests {

    @Test
    void conflictException_shouldCreateWithMessage() {
        String message = "Произошел конфликт";
        ConflictException exception = new ConflictException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void conflictException_shouldCreateWithMessageOnly() {
        String message = "Произошел конфликт";
        ConflictException exception = new ConflictException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void notFoundException_shouldCreateWithMessage() {
        String message = "Не найдено";
        NotFoundException exception = new NotFoundException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void notFoundException_shouldCreateWithMessageOnly() {
        String message = "Не найдено";
        NotFoundException exception = new NotFoundException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void validationException_shouldCreateWithMessage() {
        String message = "Ошибка валидации";
        ValidationException exception = new ValidationException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void validationException_shouldCreateWithMessageOnly() {
        String message = "Ошибка валидации";
        ValidationException exception = new ValidationException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}

