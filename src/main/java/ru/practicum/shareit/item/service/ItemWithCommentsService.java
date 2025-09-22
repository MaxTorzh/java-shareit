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
        Item item = itemService.getItemById(itemId);

        ItemWithBookingsDto itemDto = itemBookingInfoService.createItemWithBookingsDto(item, userId);

        List<CommentDto> comments = commentService.getCommentsByItemId(itemId);
        itemDto.setComments(comments);

        return itemDto;
    }

    public List<ItemWithBookingsDto> getUserItemsWithBookingsAndComments(Long userId, Pageable pageable) {
        Page<Item> itemPage = itemService.getUserItems(userId, pageable);

        List<ItemWithBookingsDto> items = itemPage.getContent().stream()
                .map(item -> itemBookingInfoService.createItemWithBookingsDto(item, userId))
                .collect(Collectors.toList());

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

