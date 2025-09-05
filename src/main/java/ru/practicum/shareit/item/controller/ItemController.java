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

    /**
     * Создание нового предмета.
     *
     * @param itemDto данные нового предмета
     * @param ownerId ID владельца предмета
     * @return созданный предмет
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
     *
     * @param itemId ID предмета
     * @param itemDto новые данные предмета
     * @param ownerId ID владельца предмета
     * @return обновленные данные предмета
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
     * Получение данных предмета по ID.
     *
     * @param itemId ID предмета
     * @return данные предмета
     */
    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        log.info("Получен запрос на получение данных предмета с ID: {}", itemId);
        return ItemMapper.toDto(itemService.getItemById(itemId));
    }

    /**
     * Получение списка предметов пользователя.
     *
     * @param ownerId ID владельца предметов
     * @return список предметов пользователя
     */
    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Получен запрос на получение списка предметов пользователя с ID: {}", ownerId);
        return itemService.getUserItems(ownerId).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Поиск предметов по тексту.
     *
     * @param text текст для поиска
     * @param userId ID пользователя (опционально)
     * @return список найденных предметов
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
     * Удаление пользователя по ID.
     *
     * @param itemId идентификатор пользователя
     */
    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        log.info("Получен запрос на удаление вещи с ID: {}", itemId);
        itemService.deleteItem(itemId);
    }
}
