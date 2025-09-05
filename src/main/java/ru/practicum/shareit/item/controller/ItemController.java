package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
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

    @PostMapping
    public ItemDto createItem(@Valid @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return ItemMapper.toDto(
                itemService.createItem(ItemMapper.toItem(itemDto, userService.getUserById(ownerId)))
        );
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@Valid @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return ItemMapper.toDto(
                itemService.updateItem(itemId, ItemMapper.toItem(itemDto, userService.getUserById(ownerId)))
        );
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        return ItemMapper.toDto(itemService.getItemById(itemId));
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getUserItems(ownerId).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text,
                                     @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        return itemService.searchItems(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}
