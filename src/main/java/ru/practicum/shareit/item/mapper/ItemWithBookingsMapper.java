package ru.practicum.shareit.item.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.service.BookingInfoService;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;

@Component
@RequiredArgsConstructor
public class ItemWithBookingsMapper {
    private final BookingInfoService bookingInfoService;

    public ItemWithBookingsDto toDto(Item item, Long userId) {
        if (item == null) {
            return null;
        }

        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());

        if (item.getOwner() != null) {
            dto.setOwnerId(item.getOwner().getId());
        }
        if (item.getRequest() != null) {
            dto.setRequestId(item.getRequest().getId());
        }
        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            bookingInfoService.getLastBooking(item.getId()).ifPresent(dto::setLastBooking);
            bookingInfoService.getNextBooking(item.getId()).ifPresent(dto::setNextBooking);
        }
        return dto;
    }
}
