package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestsDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private final UserService userService;
    private final ItemRequestMapper itemRequestMapper;

    /**
     * Создание нового запроса на предмет.
     */
    @PostMapping
    public ItemRequestsDto createItemRequest(
            @RequestBody ItemRequestRequestDto requestDto,
            @RequestHeader("X-Sharer-User-Id") long requesterId) {

        log.info("Получен запрос на создание запроса от пользователя с ID: {}", requesterId);
        return itemRequestMapper.toDto(
                itemRequestService.createRequest(itemRequestMapper.toEntity(requestDto,
                        userService.getUserById(requesterId)))
        );
    }

    /**
     * Получение данных о запросе по ID.
     *
     * @param requestId ID запроса
     * @param userId ID пользователя, запрашивающего данные
     * @return данные запроса
     */
    @GetMapping("/{requestId}")
    public ItemRequestsDto getRequestById(
            @PathVariable long requestId,
            @RequestHeader("X-Sharer-User-Id") long userId) {

        log.info("Получен запрос на получение данных запроса с ID: {} от пользователя с ID: {}", requestId, userId);
        return itemRequestService.getItemRequestDtoById(requestId);
    }

    /**
     * Получение списка запросов пользователя.
     *
     * @param userId ID пользователя
     * @return список запросов пользователя
     */
    @GetMapping
    public List<ItemRequestsDto> getUserRequests(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получен запрос на получение списка запросов пользователя с ID: {}", userId);
        Pageable pageable = PageRequest.of(from / size, size);
        return itemRequestService.getUserRequestsDtoList(userId, pageable);
    }

    /**
     * Получение списка всех доступных запросов.
     *
     * @param userId ID пользователя, запрашивающего данные
     * @return список всех запросов
     */
    @GetMapping("/all")
    public List<ItemRequestsDto> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получен запрос на получение списка всех доступных запросов от пользователя с ID: {}", userId);
        Pageable pageable = PageRequest.of(from / size, size);
        return itemRequestService.getAllRequestsDtoList(pageable);
    }

    /**
     * Получение списка запросов других пользователей.
     *
     * @param userId ID пользователя, запрашивающего данные
     * @return список запросов других пользователей
     */
    @GetMapping("/other")
    public List<ItemRequestsDto> getOtherUserRequests(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получен запрос на получение списка запросов других пользователей от пользователя с ID: {}", userId);
        Pageable pageable = PageRequest.of(from / size, size);
        return itemRequestService.getOtherUserRequestsDtoList(userId, pageable);
    }
}
