package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Создание нового пользователя.
     *
     * @param userDto данные нового пользователя
     * @return созданный пользователь
     */
    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Получен запрос на создание нового пользователя");
        return UserMapper.toDto(
                userService.createUser(UserMapper.toUser(userDto))
        );
    }

    /**
     * Обновление данных существующего пользователя.
     *
     * @param userId идентификатор пользователя
     * @param userDto новые данные пользователя
     * @return обновленные данные пользователя
     */
    @PatchMapping("/{userId}")
    public UserDto updateUser(@Valid
                              @PathVariable Long userId,
                              @RequestBody UserDto userDto) {
        log.info("Получен запрос на обновление данных пользователя с ID: {}", userId);
        return UserMapper.toDto(
                userService.updateUser(userId, UserMapper.toUser(userDto))
        );
    }

    /**
     * Получение данных пользователя по ID.
     *
     * @param userId идентификатор пользователя
     * @return данные пользователя
     */
    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("Получен запрос на получение данных пользователя с ID: {}", userId);
        return UserMapper.toDto(userService.getUserById(userId));
    }

    /**
     * Получение списка всех пользователей.
     *
     * @return список всех пользователей
     */
    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Получен запрос на получение списка всех пользователей");;
        return userService.getAllUsers().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Удаление пользователя по ID.
     *
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Получен запрос на удаление пользователя с ID: {}", userId);
        userService.deleteUser(userId);
    }
}
