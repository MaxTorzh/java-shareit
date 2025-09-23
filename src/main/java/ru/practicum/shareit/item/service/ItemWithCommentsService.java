// ItemWithCommentsService.java
package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemWithCommentsService {
    private final ItemService itemService;
    private final CommentService commentService;
    private final ItemBookingInfoService itemBookingInfoService;

    public ItemWithBookingsDto getItemWithBookingsAndComments(Long itemId, Long userId) {
        // Получаем предмет по ID
        Item item = itemService.getItemById(itemId);

        // Создаем DTO с информацией о бронированиях
        ItemWithBookingsDto itemDto = itemBookingInfoService.createItemWithBookingsDto(item, userId);

        // Получаем и устанавливаем комментарии (без пагинации)
        List<CommentDto> comments = commentService.getCommentsByItemId(itemId, Pageable.unpaged()).getContent();
        itemDto.setComments(comments);

        return itemDto;
    }

    public List<ItemWithBookingsDto> getUserItemsWithBookingsAndComments(Long userId, Pageable pageable) {
        // Получаем страницу предметов пользователя
        Page<Item> itemPage = itemService.getUserItems(userId, pageable);

        // Преобразуем предметы в DTO с информацией о бронированиях
        List<ItemWithBookingsDto> items = itemPage.getContent().stream()
                .map(item -> itemBookingInfoService.createItemWithBookingsDto(item, userId))
                .collect(Collectors.toList());

        // Получаем ID всех предметов
        List<Long> itemIds = items.stream()
                .map(ItemWithBookingsDto::getId)
                .collect(Collectors.toList());

        // Получаем все комментарии для этих предметов (без пагинации)
        List<CommentDto> allComments = commentService.getCommentsByItemIds(itemIds, Pageable.unpaged()).getContent();

        // Группируем комментарии по ID предметов
        Map<Long, List<CommentDto>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(CommentDto::getItemId));

        // Добавляем комментарии к каждому предмету
        items.forEach(item -> {
            List<CommentDto> itemComments = commentsByItemId.getOrDefault(item.getId(), List.of());
            item.setComments(itemComments);
        });

        return items;
    }

    // Метод для обратной совместимости без пагинации
    public List<ItemWithBookingsDto> getUserItemsWithBookingsAndComments(Long userId) {
        return getUserItemsWithBookingsAndComments(userId, Pageable.unpaged());
    }
}



