package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.service.BookingService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {GlobalExceptionHandlerTest.TestController.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GlobalExceptionHandler exceptionHandler;

    @MockBean
    private BookingService bookingService;

    @RestController
    static class TestController {
        @GetMapping("/test-conflict")
        public void testConflict() {
            throw new ConflictException("Conflict test message");
        }

        @GetMapping("/test-not-found")
        public void testNotFound() {
            throw new NotFoundException("Not found test message");
        }

        @GetMapping("/test-access-denied")
        public void testAccessDenied() {
            throw new AccessDeniedException("Access denied test message");
        }

        @GetMapping("/test-illegal-argument")
        public void testIllegalArgument() {
            throw new IllegalArgumentException("Illegal argument test message");
        }

        @GetMapping("/test-validation")
        public void testValidation() {
            throw new ValidationException("Validation test message");
        }

        @GetMapping("/test-internal-error")
        public void testInternalError() {
            throw new RuntimeException("Internal error test message");
        }
    }

    /**
     * Тест обработки ConflictException.
     * Проверяет корректную обработку ConflictException и формирование правильного ответа.
     */
    @Test
    void handleConflictException_shouldReturnConflictResponse() {
        ConflictException exception = new ConflictException("Conflict test message");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflictException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Conflict test message", response.getBody().getError());
    }

    /**
     * Тест обработки NotFoundException.
     * Проверяет корректную обработку NotFoundException и формирование правильного ответа.
     */
    @Test
    void handleNotFoundException_shouldReturnNotFoundResponse() {
        NotFoundException exception = new NotFoundException("Not found test message");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFoundException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not found test message", response.getBody().getError());
    }

    /**
     * Тест обработки AccessDeniedException.
     * Проверяет корректную обработку AccessDeniedException и формирование правильного ответа.
     */
    @Test
    void handleAccessDeniedException_shouldReturnForbiddenResponse() {
        AccessDeniedException exception = new AccessDeniedException("Access denied test message");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied test message", response.getBody().getError());
    }

    /**
     * Тест обработки IllegalArgumentException.
     * Проверяет корректную обработку IllegalArgumentException и формирование правильного ответа.
     */
    @Test
    void handleIllegalArgumentException_shouldReturnBadRequestResponse() {
        IllegalArgumentException exception = new IllegalArgumentException("Illegal argument test message");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Illegal argument test message", response.getBody().getError());
    }

    /**
     * Тест обработки ValidationException.
     * Проверяет корректную обработку ValidationException и формирование правильного ответа.
     */
    @Test
    void handleValidationException_shouldReturnBadRequestResponse() {
        ValidationException exception = new ValidationException("Validation test message");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation test message", response.getBody().getError());
    }

    /**
     * Тест обработки общих исключений.
     * Проверяет корректную обработку неизвестных исключений и формирование правильного ответа.
     */
    @Test
    void handleInternalError_shouldReturnInternalServerErrorResponse() {
        Exception exception = new Exception("Internal error test message");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInternalError(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Внутренняя ошибка сервера.", response.getBody().getError());
    }

    /**
     * Тест обработки NullPointerException.
     * Проверяет корректную обработку NullPointerException как общего исключения.
     */
    @Test
    void handleInternalError_shouldHandleNullPointerException() {
        NullPointerException exception = new NullPointerException("Null pointer test message");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInternalError(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Внутренняя ошибка сервера.", response.getBody().getError());
    }

    /**
     * Тест обработки общих исключений через MockMvc.
     * Проверяет интеграцию обработчика исключений с Spring MVC.
     */
    @Test
    void handleInternalError_shouldWorkWithMockMvc() throws Exception {
        mockMvc.perform(get("/test-internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера."));
    }

    /**
     * Тест ErrorResponse.
     * Проверяет корректность работы класса ErrorResponse.
     */
    @Test
    void errorResponse_shouldSetAndGetError() {
        ErrorResponse errorResponse = new ErrorResponse("Test error message");

        assertEquals("Test error message", errorResponse.getError());
    }

    /**
     * Тест ErrorResponse с null сообщением.
     * Проверяет обработку null значений.
     */
    @Test
    void errorResponse_shouldHandleNullError() {
        ErrorResponse errorResponse = new ErrorResponse(null);

        assertNull(errorResponse.getError());
    }

    /**
     * Тест ErrorResponse с пустым сообщением.
     * Проверяет обработку пустых значений.
     */
    @Test
    void errorResponse_shouldHandleEmptyError() {
        ErrorResponse errorResponse = new ErrorResponse("");

        assertEquals("", errorResponse.getError());
    }

    /**
     * Тест обработки исключения с длинным сообщением.
     * Проверяет обработку длинных сообщений об ошибках.
     */
    @Test
    void handleException_shouldHandleLongErrorMessage() {
        String longMessage = "A".repeat(1000);
        ConflictException exception = new ConflictException(longMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflictException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(longMessage, response.getBody().getError());
    }

    /**
     * Тест обработки исключения с сообщением содержащим специальные символы.
     * Проверяет обработку специальных символов в сообщениях об ошибках.
     */
    @Test
    void handleException_shouldHandleSpecialCharactersInErrorMessage() {
        String specialMessage = "Error with special chars: \n\t\"'\\";
        ConflictException exception = new ConflictException(specialMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflictException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(specialMessage, response.getBody().getError());
    }

    /**
     * Тест обработки нескольких последовательных исключений.
     * Проверяет корректную работу обработчика при множественных вызовах.
     */
    @Test
    void handleException_shouldWorkWithMultipleCalls() {
        ConflictException conflictException = new ConflictException("Conflict message");
        NotFoundException notFoundException = new NotFoundException("Not found message");

        ResponseEntity<ErrorResponse> conflictResponse = exceptionHandler.handleConflictException(conflictException);
        ResponseEntity<ErrorResponse> notFoundResponse = exceptionHandler.handleNotFoundException(notFoundException);

        assertEquals(HttpStatus.CONFLICT, conflictResponse.getStatusCode());
        assertEquals("Conflict message", conflictResponse.getBody().getError());

        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
        assertEquals("Not found message", notFoundResponse.getBody().getError());
    }

    /**
     * Тест обработки исключения без сообщения.
     * Проверяет обработку исключений с пустым сообщением.
     */
    @Test
    void handleException_shouldHandleExceptionWithoutMessage() {
        ConflictException exception = new ConflictException(null);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflictException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getError());
    }

    /**
     * Тест обработки исключения с многострочным сообщением.
     * Проверяет обработку многострочных сообщений об ошибках.
     */
    @Test
    void handleException_shouldHandleMultilineErrorMessage() {
        String multilineMessage = "First line\nSecond line\nThird line";
        ConflictException exception = new ConflictException(multilineMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflictException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(multilineMessage, response.getBody().getError());
    }
}

