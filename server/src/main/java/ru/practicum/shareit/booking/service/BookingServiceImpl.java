package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.booking.validator.BookingValidator;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingValidator validator;

    @Override
    @Transactional
    public Booking createBooking(Booking booking) {
        validator.validateBookingCreation(booking);
        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
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
    public Page<Booking> getUserBookings(Long userId, String state, Pageable pageable) {
        validator.validateUserExists(userId);
        return bookingRepository.findByBookerIdAndState(userId, state, LocalDateTime.now(), pageable);
    }

    @Override
    public Page<Booking> getOwnerBookings(Long ownerId, String state, Pageable pageable) {
        validator.validateUserExists(ownerId);
        return bookingRepository.findByItemOwnerIdAndState(ownerId, state, LocalDateTime.now(), pageable);
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
