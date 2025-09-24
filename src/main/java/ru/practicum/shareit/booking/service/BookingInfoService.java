package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDateDto;
import ru.practicum.shareit.booking.status.BookingStatus;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingInfoService {
    private final BookingRepository bookingRepository;

    public Optional<BookingDateDto> getLastBooking(Long itemId) {
        return bookingRepository.findLastBooking(itemId, LocalDateTime.now(), BookingStatus.APPROVED)
                .map(this::convertToBookingDateDto);
    }

    public Optional<BookingDateDto> getNextBooking(Long itemId) {
        return bookingRepository.findNextBooking(itemId, LocalDateTime.now(), BookingStatus.APPROVED)
                .map(this::convertToBookingDateDto);
    }

    private BookingDateDto convertToBookingDateDto(Booking booking) {
        return new BookingDateDto(
                booking.getId(),
                booking.getBooker().getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }
}
