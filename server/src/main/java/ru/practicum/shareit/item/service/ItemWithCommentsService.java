package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.service.BookingInfoService;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemWithCommentsService {
    private final ItemService itemService;
    private final CommentService commentService;
    private final ItemMapper itemMapper;
    private final BookingInfoService bookingInfoService;

    public ItemWithBookingsDto getItemWithBookingsAndComments(Long itemId, Long userId) {
        Item item = itemService.getItemByIdWithDependencies(itemId);
        return createItemWithBookingsDto(item, userId);
    }

    public ItemWithBookingsDto createItemWithBookingsDto(Item item, Long userId) {
        ItemWithBookingsDto itemDto = itemMapper.toWithBookingsDto(item);
        addBookingInfo(itemDto, item, userId);
        List<CommentDto> comments = commentService.getCommentsByItemId(item.getId(), Pageable.unpaged()).getContent();
        itemDto.setComments(comments);
        return itemDto;
    }

    public void addBookingInfo(ItemWithBookingsDto dto, Item item, Long userId) {
        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            bookingInfoService.getLastBooking(item.getId()).ifPresent(dto::setLastBooking);
            bookingInfoService.getNextBooking(item.getId()).ifPresent(dto::setNextBooking);
        }
    }

    public List<ItemWithBookingsDto> getUserItemsWithBookingsAndComments(Long ownerId, Pageable pageable) {
        Page<Item> itemsPage = itemService.getUserItems(ownerId, pageable);

        List<Long> itemIds = itemsPage.getContent().stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Item> itemsWithDependencies = itemService.getItemsWithDependencies(itemIds);

        return itemsWithDependencies.stream()
                .map(item -> createItemWithBookingsDto(item, ownerId))
                .collect(Collectors.toList());
    }
}

