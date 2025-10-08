package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.comment.dto.CommentDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.shareit.item.validator.ItemValidator;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;
    private final ItemValidator validator;

    /**
     * Создание новой вещи от имени указанного пользователя.
     *
     * @param ownerId идентификатор пользователя-владельца вещи
     * @param itemRequestDto данные вещи для создания
     * @return созданная вещь в формате ResponseEntity
     */
    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @RequestBody @Valid ItemRequestDto itemRequestDto) {

        validator.validateItemCreation(itemRequestDto);
        log.info("Creating item {} for user {}", itemRequestDto, ownerId);
        return itemClient.createItem(ownerId, itemRequestDto);
    }

    /**
     * Обновление информации о вещи.
     *
     * @param ownerId идентификатор владельца вещи
     * @param itemId идентификатор вещи для обновления
     * @param itemRequestDto данные вещи для обновления
     * @return обновленная вещь в формате ResponseEntity
     */
    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @PathVariable Long itemId,
            @RequestBody @Valid ItemRequestDto itemRequestDto) {

        validator.validateItemId(itemId);
        log.info("Updating item with id {} for user {}", itemId, ownerId);
        return itemClient.updateItem(ownerId, itemId, itemRequestDto);
    }

    /**
     * Получение информации о вещи по её идентификатору.
     * Если запрос выполняется владельцем вещи, возвращается расширенная информация с бронированиями.
     *
     * @param userId идентификатор пользователя, запрашивающего информацию
     * @param itemId идентификатор вещи
     * @return информация о вещи в формате ResponseEntity
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemWithBookings(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable Long itemId) {

        validator.validateItemId(itemId);
        log.info("Getting item with id {} for user {}", itemId, userId);
        return itemClient.getItemWithBookings(userId, itemId);
    }

    /**
     * Получение списка всех вещей пользователя с информацией о бронированиях.
     *
     * @param ownerId идентификатор владельца вещей
     * @param from индекс первого элемента для пагинации (начиная с 0)
     * @param size количество элементов для пагинации (больше 0)
     * @return список вещей пользователя в формате ResponseEntity
     */
    @GetMapping
    public ResponseEntity<Object> getUserItemsWithBookings(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {

        log.info("Getting items for user {} with from={} and size={}", ownerId, from, size);
        return itemClient.getUserItemsWithBookings(ownerId, from, size);
    }

    /**
     * Поиск вещей по тексту в названии или описании.
     *
     * @param text текст для поиска
     * @param from индекс первого элемента для пагинации (начиная с 0)
     * @param size количество элементов для пагинации (больше 0)
     * @param userId идентификатор пользователя, выполняющего поиск (может отсутствовать)
     * @return список найденных вещей в формате ResponseEntity
     */
    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @RequestParam String text,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {

        validator.validateSearchText(text);
        log.info("Searching items with text '{}' from={} size={}", text, from, size);
        return itemClient.searchItems(text, from, size, userId);
    }

    /**
     * Удаление вещи по её идентификатору.
     *
     * @param itemId идентификатор вещи для удаления
     * @return результат операции в формате ResponseEntity
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(
            @PathVariable Long itemId) {

        validator.validateItemId(itemId);
        log.info("Deleting item with id {}", itemId);
        return itemClient.deleteItem(itemId);
    }

    /**
     * Создание комментариев к вещи.
     *
     * @param itemId идентификатор вещи, к которой добавляется комментарий
     * @param authorId идентификатор пользователя-автора комментария
     * @param commentDto данные комментария
     * @return созданный комментарий в формате ResponseEntity
     */
    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") long authorId,
            @RequestBody @Valid CommentDto commentDto) {

        validator.validateItemId(itemId);
        validator.validateComment(commentDto);
        log.info("Creating comment for item {} from user {}", itemId, authorId);
        return itemClient.createComment(authorId, itemId, commentDto);
    }
}

