package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.shareit.user.validator.UserValidator;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;
    private final UserValidator validator;

    /**
     * Создание нового пользователя.
     *
     * @param userDto данные пользователя для создания
     * @return созданный пользователь в формате ResponseEntity
     */
    @PostMapping
    public ResponseEntity<Object> createUser(
            @Valid @RequestBody UserDto userDto) {
        validator.validateUserCreation(userDto);

        log.info("Creating user {}", userDto);
        return userClient.createUser(userDto);
    }

    /**
     * Обновление информации о пользователе.
     *
     * @param userId идентификатор пользователя для обновления
     * @param userDto данные пользователя для обновления
     * @return обновленный пользователь в формате ResponseEntity
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(
            @PathVariable Long userId,
            @RequestBody UserDto userDto) {
        validator.validateUserUpdate(userId, userDto);

        log.info("Updating user with id {}", userId);
        return userClient.updateUser(userId, userDto);
    }

    /**
     * Получение информации о пользователе по его идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return информация о пользователе в формате ResponseEntity
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(
            @PathVariable Long userId) {

        log.info("Getting user by id {}", userId);
        return userClient.getUserById(userId);
    }

    /**
     * Получение списка всех пользователей с пагинацией.
     *
     * @param from индекс первого элемента для пагинации (начиная с 0)
     * @param size количество элементов для пагинации (больше 0)
     * @return список пользователей в формате ResponseEntity
     */
    @GetMapping
    public ResponseEntity<Object> getAllUsers(
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {

        log.info("Getting all users with from={} and size={}", from, size);
        return userClient.getAllUsers(from, size);
    }

    /**
     * Удаление пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя для удаления
     * @return результат операции в формате ResponseEntity
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(
            @PathVariable Long userId) {

        log.info("Deleting user with id {}", userId);
        return userClient.deleteUser(userId);
    }
}

