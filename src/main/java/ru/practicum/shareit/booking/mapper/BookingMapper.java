package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingListDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "status", target = "status", qualifiedByName = "bookingStatusToString")
    @Mapping(source = "booker.id", target = "booker.id")
    @Mapping(source = "booker.name", target = "booker.name")
    @Mapping(source = "booker.email", target = "booker.email")
    @Mapping(source = "item.id", target = "item.id")
    @Mapping(source = "item.name", target = "item.name")
    @Mapping(source = "item.description", target = "item.description")
    @Mapping(source = "item.available", target = "item.available")
    @Mapping(source = "item.request.id", target = "item.requestId")
    BookingDto toDto(Booking booking);

    @Mapping(source = "status", target = "status", qualifiedByName = "bookingStatusToString")
    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.name", target = "itemName")
    @Mapping(source = "booker.id", target = "bookerId")
    @Mapping(source = "booker.name", target = "bookerName")
    BookingListDto toListDto(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "WAITING")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "booker", source = "booker")
    Booking toBooking(BookingRequestDto bookingRequestDto, Item item, User booker);

    @Named("bookingStatusToString")
    default String bookingStatusToString(BookingStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToBookingStatus")
    default BookingStatus stringToBookingStatus(String status) {
        if (status == null) return BookingStatus.WAITING;
        try {
            return BookingStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return BookingStatus.WAITING;
        }
    }
}
