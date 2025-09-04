package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Repository
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public Booking createBooking(Booking booking) {
        validateBooking(booking);
        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking approveBooking(Long bookingId, Boolean approved) {
        Booking booking = getBookingById(bookingId);
        updateBookingStatus(booking, approved);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с таким ID " + bookingId + " не найдена"));
    }

    @Override
    public List<Booking> getUserBookings(Long userId) {
        userService.getUserById(userId);
        return bookingRepository.findByBookerId(userId);
    }

    @Override
    public List<Booking> getOwnerBookings(Long ownerId) {
        userService.getUserById(ownerId);
        return bookingRepository.findByItemOwnerId(ownerId);
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private void validateBooking(Booking booking) {
        userService.getUserById(booking.getBooker().getId());
        itemService.getItemById(booking.getItem().getId());
        if (!booking.getItem().getIsAvailable()) {
            throw new AccessDeniedException("Вещь недоступна для бронирования");
        }
        if (booking.getStartTime().isAfter(booking.getEndTime())) {
            throw new ValidationException("Дата начала пользования должна быть до даты окончания");
        }
    }

    private void updateBookingStatus(Booking booking, Boolean approved) {
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
    }
}
