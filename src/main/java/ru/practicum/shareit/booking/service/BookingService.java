package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;

public interface BookingService {

    Booking createBooking(Booking booking);

    Booking approveBooking(Long bookingId, Boolean approved, Long ownerId);

    Booking getBookingById(Long bookingId);

    Booking getBookingByIdWithAccessCheck(Long bookingId, Long userId);

    Page<Booking> getUserBookings(Long userId, Pageable pageable);

    Page<Booking> getOwnerBookings(Long ownerId, Pageable pageable);

    void cancelBooking(Long bookingId, Long userId);
}
