package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {

    Booking createBooking(Booking booking);

    Booking approveBooking(Long bookingId, Boolean approved, Long ownerId);

    Booking getBookingById(Long bookingId);

    Booking getBookingByIdWithAccessCheck(Long bookingId, Long userId);

    List<Booking> getUserBookings(Long userId);

    List<Booking> getOwnerBookings(Long ownerId);

    void cancelBooking(Long bookingId, Long userId);
}
