package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
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

    /**
     * Создание нового предмета.
     */
    @PostMapping
    public ItemDto createItem(@Valid @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Получен запрос на создание нового предмета от пользователя с ID: {}", ownerId);
        return ItemMapper.toDto(
                itemService.createItem(ItemMapper.toItem(itemDto, userService.getUserById(ownerId)))
        );
    }

    /**
     * Обновление данных существующего предмета.
     */
    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@Valid @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Получен запрос на обновление данных предмета с ID: {} от пользователя с ID: {}", itemId, ownerId);
        return ItemMapper.toDto(
                itemService.updateItem(itemId, ItemMapper.toItem(itemDto, userService.getUserById(ownerId)))
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
        return itemService.getItemWithBookings(itemId, userId);
    }

    /**
     * Получение списка предметов пользователя с бронированиями.
     * Заменяет старый эндпоинт getUserItems.
     */
    @GetMapping
    public List<ItemWithBookingsDto> getUserItemsWithBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Получен запрос на получение списка предметов пользователя с ID: {}", ownerId);
        return itemService.getUserItemsWithBookings(ownerId);
    }

    /**
     * Поиск предметов по тексту.
     */
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text,
                                     @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        log.info("Получен запрос на поиск предметов по тексту: '{}'", text);
        return itemService.searchItems(text).stream()
                .map(ItemMapper::toDto)
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

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto commentRequestDto,
                                 @RequestHeader("X-Sharer-User-Id") Long authorId) {
        return commentService.createComment(itemId, commentRequestDto, authorId);
    }
}
