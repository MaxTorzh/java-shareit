package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.booking.validator.BookingValidator;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingValidator validator;

    @Override
    public Booking createBooking(Booking booking) {
        validator.validateBookingCreation(booking);
        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        Booking booking = getBookingById(bookingId);

        validator.validateOwnerRights(booking, ownerId);
        validator.validateBookingStatusForApproval(booking);

        updateBookingStatus(booking, approved);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с таким ID " + bookingId + " не найдена"));
    }

    @Override
    public Booking getBookingByIdWithAccessCheck(Long bookingId, Long userId) {
        Booking booking = getBookingById(bookingId);
        validator.validateBookingAccess(booking, userId);
        return booking;
    }

    @Override
    public List<Booking> getUserBookings(Long userId) {
        validator.validateUserExists(userId);
        return bookingRepository.findByBookerId(userId);
    }

    @Override
    public List<Booking> getOwnerBookings(Long ownerId) {
        validator.validateUserExists(ownerId);
        return bookingRepository.findByItemOwnerId(ownerId);
    }

    @Override
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = getBookingById(bookingId);

        validator.validateBookerRights(booking, userId);
        validator.validateBookingStatusForCancellation(booking);

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private void updateBookingStatus(Booking booking, Boolean approved) {
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
    }
}
