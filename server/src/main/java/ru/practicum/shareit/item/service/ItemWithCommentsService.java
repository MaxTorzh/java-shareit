package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.service.BookingInfoService;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemWithCommentsService {
    private final ItemService itemService;
    private final CommentService commentService;
    private final ItemMapper itemMapper;
    private final BookingInfoService bookingInfoService;

    public ItemWithBookingsDto getItemWithBookingsAndComments(Long itemId, Long userId) {
        Item item = itemService.getItemByIdWithDependencies(itemId);
        ItemWithBookingsDto itemDto = itemMapper.toWithBookingsDto(item);
        addBookingInfo(itemDto, item, userId);
        List<CommentDto> comments = commentService.getCommentsByItemId(itemId, Pageable.unpaged()).getContent();
        itemDto.setComments(comments);
        return itemDto;
    }

    private void addBookingInfo(ItemWithBookingsDto dto, Item item, Long userId) {
        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            bookingInfoService.getLastBooking(item.getId()).ifPresent(dto::setLastBooking);
            bookingInfoService.getNextBooking(item.getId()).ifPresent(dto::setNextBooking);
        }
    }
}
