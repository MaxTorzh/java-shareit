package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

public class BookingMapper {
    private static BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        Optional.ofNullable(booking.getItem())
                .ifPresent(item -> dto.setItemId(item.getId()));
        Optional.ofNullable(booking.getBooker())
                .ifPresent(booker -> dto.setBookerId(booker.getId()));
        Optional.ofNullable(booking.getStatus())
                .ifPresent(status -> dto.setStatus(status.name()));
        return dto;
    }

    private static Booking toBooking(BookingDto dto, Item item, User booker) {
        if (dto == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setId(dto.getId());
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(dto.getEndTime());
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
