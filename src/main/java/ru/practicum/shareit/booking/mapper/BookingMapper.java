package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

public class BookingMapper {
    public static BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        Optional.ofNullable(booking.getStatus())
                .ifPresent(status -> dto.setStatus(status.name()));
        if (booking.getBooker() != null) {
            UserDto bookerDto = new UserDto();
            bookerDto.setId(booking.getBooker().getId());
            bookerDto.setName(booking.getBooker().getName());
            bookerDto.setEmail(booking.getBooker().getEmail());
            dto.setBooker(bookerDto);
        }
        if (booking.getItem() != null) {
            ItemDto itemDto = new ItemDto();
            itemDto.setId(booking.getItem().getId());
            itemDto.setName(booking.getItem().getName());
            itemDto.setDescription(booking.getItem().getDescription());
            itemDto.setAvailable(booking.getItem().getAvailable());
            if (booking.getItem().getRequest() != null) {
                itemDto.setRequestId(booking.getItem().getRequest().getId());
            }
            dto.setItem(itemDto);
        }
        return dto;
    }

    public static Booking toBooking(BookingDto dto, Item item, User booker) {
        if (dto == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setId(dto.getId());
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        if (dto.getStatus() != null) {
            try {
                booking.setStatus(BookingStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                booking.setStatus(BookingStatus.WAITING);
            }
        } else {
            booking.setStatus(BookingStatus.WAITING);
        }
        return booking;
    }
}
