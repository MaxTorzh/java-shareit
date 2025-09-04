package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {

    Booking createBooking(Booking booking);

    Booking approveBooking(Long bookingId, Boolean approved);

    Booking getBookingById(Long bookingId);

    List<Booking> getUserBookings(Long userId);

    List<Booking> getOwnerBookings(Long ownerId);

    void cancelBooking(Long bookingId);
}
