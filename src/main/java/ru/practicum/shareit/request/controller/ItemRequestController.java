package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private final UserService userService;

    /**
     * Создание нового запроса на предмет.
     *
     * @param itemRequestDto данные нового запроса
     * @param requesterId ID пользователя, создающего запрос
     * @return созданный запрос
     */
    @PostMapping
    public ItemRequestDto createItemRequest(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                            @RequestHeader("X-Sharer-User-Id") Long requesterId) {
        log.info("Получен запрос на создание запроса от пользователя с ID: {}", requesterId);
        return ItemRequestMapper.toDto(
                itemRequestService.createRequest(ItemRequestMapper.toItemRequest(itemRequestDto,
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
    public ItemRequestDto getRequestById(@PathVariable Long requestId,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на получение данных запроса с ID: {} от пользователя с ID: {}", requestId, userId);
        return ItemRequestMapper.toDto(itemRequestService.getRequestById(requestId));
    }

    /**
     * Получение списка запросов пользователя.
     *
     * @param userId ID пользователя
     * @return список запросов пользователя
     */
    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на получение списка запросов пользователя с ID: {}", userId);
        return itemRequestService.getUserRequests(userId).stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение списка всех доступных запросов.
     *
     * @param userId ID пользователя, запрашивающего данные
     * @return список всех запросов
     */
    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на получение списка всех доступных запросов от пользователя с ID: {}", userId);
        return itemRequestService.getAllRequests().stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }
}
