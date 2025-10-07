package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.shareit.request.validator.ItemRequestValidator;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {

    private final ItemRequestClient requestClient;
    private final ItemRequestValidator validator;

    /**
     * Создание нового запроса на вещь от имени указанного пользователя.
     *
     * @param requesterId идентификатор пользователя, создающего запрос
     * @param requestDto данные запроса на вещь
     * @return созданный запрос на вещь в формате ResponseEntity
     */
    @PostMapping
    public ResponseEntity<Object> createItemRequest(
            @RequestHeader("X-Sharer-User-Id") long requesterId,
            @Valid @RequestBody ItemRequestRequestDto requestDto) {

        validator.validateItemRequestCreation(requestDto);
        log.info("Creating item request {} for user {}", requestDto, requesterId);
        return requestClient.createItemRequest(requesterId, requestDto);
    }

    /**
     * Получение информации о запросе на вещь по его идентификатору.
     *
     * @param userId идентификатор пользователя, запрашивающего информацию
     * @param requestId идентификатор запроса на вещь
     * @return информация о запросе на вещь в формате ResponseEntity
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable Long requestId) {

        validator.validateRequestId(requestId);
        log.info("Getting request with id {} for user {}", requestId, userId);
        return requestClient.getRequestById(userId, requestId);
    }

    /**
     * Получение списка всех запросов текущего пользователя с информацией о вещах.
     *
     * @param userId идентификатор пользователя, чьи запросы запрашиваются
     * @param from индекс первого элемента для пагинации (начиная с 0)
     * @param size количество элементов для пагинации (больше 0)
     * @return список запросов пользователя в формате ResponseEntity
     */
    @GetMapping
    public ResponseEntity<Object> getUserRequests(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {

        validator.validatePagination(from, size);
        log.info("Getting user requests for user {} with from={} and size={}", userId, from, size);
        return requestClient.getUserRequests(userId, from, size);
    }

    /**
     * Получение списка всех запросов на вещи, доступных в системе.
     *
     * @param userId идентификатор пользователя, запрашивающего список
     * @param from индекс первого элемента для пагинации (начиная с 0)
     * @param size количество элементов для пагинации (больше 0)
     * @return список всех запросов на вещи в формате ResponseEntity
     */
    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {

        validator.validatePagination(from, size);
        log.info("Getting all requests for user {} with from={} and size={}", userId, from, size);
        return requestClient.getAllRequests(userId, from, size);
    }

    /**
     * Получение списка запросов на вещи других пользователей (исключая запросы текущего пользователя).
     *
     * @param userId идентификатор пользователя, запрашивающего список
     * @param from индекс первого элемента для пагинации (начиная с 0)
     * @param size количество элементов для пагинации (больше 0)
     * @return список запросов других пользователей в формате ResponseEntity
     */
    @GetMapping("/other")
    public ResponseEntity<Object> getOtherUserRequests(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {

        validator.validatePagination(from, size);
        log.info("Getting other user requests for user {} with from={} and size={}", userId, from, size);
        return requestClient.getOtherUserRequests(userId, from, size);
    }
}

