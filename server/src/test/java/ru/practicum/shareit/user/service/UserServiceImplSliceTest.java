package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.validator.UserValidator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Slice тесты для сервиса пользователей {@link UserServiceImpl}.
 * Тестируют функциональность сервиса с использованием реальной базы данных
 * без внешних зависимостей (сервис не имеет внешних зависимостей).
 *
 * Используют @DataJpaTest для тестирования слоя работы с БД и @Import для
 * загрузки тестируемого сервиса и его зависимостей.
 *
 * Класс использует аннотацию {@link DirtiesContext} для очистки контекста
 * после каждого теста, обеспечивая независимость тестов друг от друга.
 *
 * Тесты проверяют как успешные сценарии, так и обработку ошибок,
 * включая конфликты по email и попытки доступа к несуществующим пользователям.
 */
@DataJpaTest
@Import({UserServiceImpl.class, UserValidator.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceImplSliceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user;

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Создает тестового пользователя для использования в тестах.
     */
    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test User");
        user.setEmail("user@test.com");
    }

    /**
     * Тест создания нового пользователя.
     * Проверяет, что пользователь успешно создается в базе данных
     * с правильными данными и присвоенным ID.
     */
    @Test
    void createUser_shouldCreateAndReturnUser() {
        User createdUser = userService.createUser(user);

        assertNotNull(createdUser.getId());
        assertEquals("Test User", createdUser.getName());
        assertEquals("user@test.com", createdUser.getEmail());
    }

    /**
     * Тест создания пользователя с уже существующим email.
     * Проверяет, что при попытке создания пользователя с email,
     * который уже используется другим пользователем, выбрасывается
     * исключение {@link ConflictException}.
     */
    @Test
    void createUser_shouldThrowConflictExceptionWhenEmailExists() {
        userService.createUser(user);

        User duplicateUser = new User();
        duplicateUser.setName("Another User");
        duplicateUser.setEmail("user@test.com");

        assertThrows(ConflictException.class, () -> userService.createUser(duplicateUser));
    }

    /**
     * Тест получения пользователя по ID.
     * Проверяет, что существующий пользователь успешно возвращается
     * с правильными данными.
     */
    @Test
    void getUserById_shouldReturnUserWhenExists() {
        User savedUser = userRepository.save(user);

        User foundUser = userService.getUserById(savedUser.getId());

        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(savedUser.getName(), foundUser.getName());
        assertEquals(savedUser.getEmail(), foundUser.getEmail());
    }

    /**
     * Тест получения несуществующего пользователя по ID.
     * Проверяет, что при попытке получить пользователя с несуществующим ID
     * выбрасывается исключение {@link NotFoundException}.
     */
    @Test
    void getUserById_shouldThrowNotFoundExceptionWhenNotExists() {
        Long nonExistingUserId = 999L;

        assertThrows(NotFoundException.class, () -> userService.getUserById(nonExistingUserId));
    }

    /**
     * Тест обновления пользователя.
     * Проверяет, что пользователь успешно обновляется с новыми данными
     * и изменения сохраняются в базе данных.
     */
    @Test
    void updateUser_shouldUpdateUserFields() {
        User savedUser = userRepository.save(user);

        User updateUser = new User();
        updateUser.setName("Updated Name");
        updateUser.setEmail("updated@test.com");

        User updatedUser = userService.updateUser(savedUser.getId(), updateUser);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@test.com", updatedUser.getEmail());
    }

    /**
     * Тест обновления пользователя с конфликтующим email.
     * Проверяет, что при попытке обновить пользователя с email,
     * который уже используется другим пользователем, выбрасывается
     * исключение {@link ConflictException}.
     */
    @Test
    void updateUser_shouldThrowConflictExceptionWhenEmailExists() {
        User savedUser = userRepository.save(user);

        User anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@test.com");
        userRepository.save(anotherUser);

        User updateUser = new User();
        updateUser.setEmail("another@test.com");

        assertThrows(ConflictException.class, () -> userService.updateUser(savedUser.getId(), updateUser));
    }

    /**
     * Тест получения списка всех пользователей.
     * Проверяет, что список пользователей успешно возвращается
     * и содержит созданных пользователей.
     */
    @Test
    void getAllUsers_shouldReturnUsersList() {
        User savedUser = userRepository.save(user);
        Pageable pageable = PageRequest.of(0, 10);

        var users = userService.getAllUsers(pageable);

        assertEquals(1, users.getTotalElements());
        assertEquals(savedUser.getId(), users.getContent().get(0).getId());
    }

    /**
     * Тест удаления пользователя.
     * Проверяет, что пользователь успешно удаляется из базы данных
     * и становится недоступен для последующего получения.
     */
    @Test
    void deleteUser_shouldRemoveUser() {
        User savedUser = userRepository.save(user);

        userService.deleteUser(savedUser.getId());

        assertThrows(NotFoundException.class, () -> userService.getUserById(savedUser.getId()));
    }

    /**
     * Тест удаления несуществующего пользователя.
     * Проверяет, что при попытке удалить пользователя с несуществующим ID
     * выбрасывается исключение {@link NotFoundException}.
     */
    @Test
    void deleteUser_shouldThrowNotFoundExceptionWhenUserNotExists() {
        Long nonExistingUserId = 999L;

        assertThrows(NotFoundException.class, () -> userService.deleteUser(nonExistingUserId));
    }
}

