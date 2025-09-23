package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemWithCommentsService;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final UserService userService;
    private final CommentService commentService;
    private final ItemWithCommentsService itemWithCommentsService;
    private final ItemMapper itemMapper;
    private final ItemRequestService itemRequestService;

    /**
     * Создание нового предмета.
     */
    @PostMapping
    public ItemDto createItem(@Valid @RequestBody ItemRequestDto itemRequestDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Получен запрос на создание нового предмета от пользователя с ID: {}", ownerId);

        return itemMapper.toDto(
                itemService.createItem(
                        itemMapper.toItem(
                                itemRequestDto,
                                userService.getUserById(ownerId),
                                itemRequestDto.getRequestId() != null ?
                                        itemRequestService.getRequestById(itemRequestDto.getRequestId()) :
                                        null
                        )
                )
        );
    }

    /**
     * Обновление данных существующего предмета.
     */
    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestBody ItemRequestDto itemRequestDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Получен запрос на обновление данных предмета с ID: {} от пользователя с ID: {}", itemId, ownerId);

        return itemMapper.toDto(
                itemService.updateItem(
                        itemId,
                        itemMapper.toItem(
                                itemRequestDto,
                                userService.getUserById(ownerId),
                                itemRequestDto.getRequestId() != null ?
                                        itemRequestService.getRequestById(itemRequestDto.getRequestId()) :
                                        null
                        )
                )
        );
    }

    /**
     * Получение данных предмета с бронированиями.
     * Заменяет старый эндпоинт getItemById.
     */
    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getItemWithBookings(@PathVariable Long itemId,
                                                   @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на получение данных предмета с ID: {}", itemId);
        return itemWithCommentsService.getItemWithBookingsAndComments(itemId, userId);
    }

    /**
     * Получение списка предметов пользователя с бронированиями.
     * Заменяет старый эндпоинт getUserItems.
     */
    @GetMapping
    public List<ItemWithBookingsDto> getUserItemsWithBookings(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение списка предметов пользователя с ID: {}", ownerId);

        Pageable pageable = PageRequest.of(from / size, size);
        return itemService.getUserItems(ownerId, pageable).getContent().stream()
                .map(item -> itemWithCommentsService.getItemWithBookingsAndComments(item.getId(), ownerId))
                .collect(Collectors.toList());
    }

    /**
     * Поиск предметов по тексту.
     */
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text,
                                     @RequestParam(defaultValue = "0") Integer from,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        log.info("Получен запрос на поиск предметов по тексту: '{}'", text);

        Pageable pageable = PageRequest.of(from / size, size);
        return itemService.searchItems(text, pageable).getContent().stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Удаление предмета по ID.
     */
    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        log.info("Получен запрос на удаление вещи с ID: {}", itemId);
        itemService.deleteItem(itemId);
    }

    /**
     * Добавление комментария.
     */
    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto commentDto,
                                 @RequestHeader("X-Sharer-User-Id") Long authorId) {
        log.info("Получен запрос на создание комментария для предмета с ID: {} от пользователя с ID: {}",
                itemId, authorId);
        return commentService.createComment(itemId, commentDto, authorId);
    }
}
