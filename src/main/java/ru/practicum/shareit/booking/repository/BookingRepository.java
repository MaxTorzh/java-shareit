package ru.practicum.shareit.booking.repository;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findByBookerId(Long bookerId);

    List<Booking> findByItemOwnerId(Long ownerId);

    List<Booking> findByItemIdAndStatus(Long itemId, BookingStatus status);

    List<Booking> findByEndBefore(LocalDateTime endTime);

    Boolean existsByItemAndStatus(Long itemId, BookingStatus status);
}
