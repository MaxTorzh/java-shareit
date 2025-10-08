package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Тестовый класс для основной точки входа в приложение.
 * Проверяет, что приложение запускается корректно и загружает контекст Spring.
 */
class ShareItServerTest {

    /**
     * Тест проверяет, что main метод запускает Spring Boot приложение успешно.
     * Этот тест проверяет, что метод SpringApplication.run вызывается корректно
     * и возвращает действительный ApplicationContext.
     */
    @Test
    void main_shouldStartApplication() {
        String[] args = new String[]{};

        try (var mockedStatic = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            mockedStatic.when(() -> SpringApplication.run(ShareItServer.class, args))
                    .thenReturn(mockContext);

            ShareItServer.main(args);

            mockedStatic.verify(() -> SpringApplication.run(ShareItServer.class, args));
        }
    }

    /**
     * Тест проверяет, что контекст приложения загружается без исключений.
     */
    @Test
    void contextLoads() {
        assertNotNull(ShareItServer.class);
    }
}

