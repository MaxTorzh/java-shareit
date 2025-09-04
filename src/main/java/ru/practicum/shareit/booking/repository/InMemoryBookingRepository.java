package ru.practicum.shareit.booking.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.BookingStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryBookingRepository implements BookingRepository {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);


    @Override
    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            booking.setId(counter.getAndIncrement());
        }
        bookings.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(bookings.get(id));
    }

    @Override
    public List<Booking> findByBookerId(Long bookerId) {
        return bookings.values().stream()
                .filter(booking -> booking.getBooker().getId().equals(bookerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByItemOwnerId(Long ownerId) {
        return bookings.values().stream()
                .filter(booking -> booking.getItem().getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByItemIdAndStatus(Long itemId, BookingStatus status) {
        return bookings.values().stream()
                .filter(booking -> booking.getItem().getId().equals(itemId) &&
                        booking.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByEndBefore(LocalDateTime endTime) {
        return bookings.values().stream()
                .filter(booking -> booking.getEndTime().isBefore(endTime))
                .collect(Collectors.toList());
    }

    @Override
    public Boolean existsByItemAndStatus(Long itemId, BookingStatus status) {
        return bookings.values().stream()
                .anyMatch(booking -> booking.getItem().getId().equals(itemId) &&
                        booking.getStatus() == status);
    }
}
