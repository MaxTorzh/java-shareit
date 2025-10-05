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

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                             @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Creating item {} for user {}", itemRequestDto, ownerId);
        return itemClient.createItem(ownerId, itemRequestDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                             @PathVariable Long itemId,
                                             @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Updating item with id {} for user {}", itemId, ownerId);
        return itemClient.updateItem(ownerId, itemId, itemRequestDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemWithBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @PathVariable Long itemId) {
        log.info("Getting item with id {} for user {}", itemId, userId);
        return itemClient.getItemWithBookings(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItemsWithBookings(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Getting items for user {} with from={} and size={}", ownerId, from, size);
        return itemClient.getUserItemsWithBookings(ownerId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text,
                                              @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                              @Positive @RequestParam(defaultValue = "10") Integer size,
                                              @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        log.info("Searching items with text '{}' from={} size={}", text, from, size);
        return itemClient.searchItems(text, from, size, userId);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@PathVariable Long itemId) {
        log.info("Deleting item with id {}", itemId);
        return itemClient.deleteItem(itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@PathVariable Long itemId,
                                                @RequestHeader("X-Sharer-User-Id") long authorId,
                                                @Valid @RequestBody CommentDto commentDto) {
        log.info("Creating comment for item {} from user {}", itemId, authorId);
        return itemClient.createComment(authorId, itemId, commentDto);
    }
}

