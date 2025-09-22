package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemWithCommentsService {
    private final ItemService itemService;
    private final CommentService commentService;

    public ItemWithBookingsDto getItemWithBookingsAndComments(Long itemId, Long userId) {
        ItemWithBookingsDto itemDto = itemService.getItemWithBookings(itemId, userId);
        List<CommentDto> comments = commentService.getCommentsByItemId(itemId);
        itemDto.setComments(comments);
        return itemDto;
    }

    public List<ItemWithBookingsDto> getUserItemsWithBookingsAndComments(Long userId) {
        List<ItemWithBookingsDto> items = itemService.getUserItemsWithBookings(userId);
        List<Long> itemIds = items.stream()
                .map(ItemWithBookingsDto::getId)
                .collect(Collectors.toList());
        List<CommentDto> allComments = commentService.getCommentsByItemIds(itemIds);
        Map<Long, List<CommentDto>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(CommentDto::getItemId));
        items.forEach(item -> {
            List<CommentDto> itemComments = commentsByItemId.getOrDefault(item.getId(), List.of());
            item.setComments(itemComments);
        });
        return items;
    }
}
