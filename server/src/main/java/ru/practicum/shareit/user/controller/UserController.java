package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final UserMapper userMapper;

    /**
     * Создание нового пользователя.
     */
    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {

        log.info("Получен запрос на создание нового пользователя");
        User user = userMapper.toEntity(userDto);
        return userMapper.toDto(service.createUser(user));
    }

    /**
     * Обновление данных существующего пользователя.
     */
    @PatchMapping("/{userId}")
    public UserDto updateUser(
            @PathVariable long userId,
            @RequestBody UserDto userDto) {

        log.info("Получен запрос на обновление данных пользователя с ID: {}", userId);
        User user = userMapper.toEntity(userDto);
        return userMapper.toDto(service.updateUser(userId, user));
    }

    /**
     * Получение данных пользователя по ID.
     */
    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable long userId) {

        log.info("Получен запрос на получение данных пользователя с ID: {}", userId);
        return userMapper.toDto(service.getUserById(userId));
    }

    /**
     * Получение списка всех пользователей.
     */
    @GetMapping
    public List<UserDto> getAllUsers(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получен запрос на получение списка всех пользователей");
        Pageable pageable = PageRequest.of(from / size, size);
        return service.getAllUsers(pageable).getContent().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Удаление пользователя по ID.
     */
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {

        log.info("Получен запрос на удаление пользователя с ID: {}", userId);
        service.deleteUser(userId);
    }
}

