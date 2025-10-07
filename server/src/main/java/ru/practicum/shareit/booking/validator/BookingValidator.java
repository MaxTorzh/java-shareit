package ru.practicum.shareit.booking.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingValidator {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    public void validateBookingCreation(Booking booking) {
        validateUserExists(booking.getBooker().getId());
        validateItemExists(booking.getItem().getId());
        validateItemAvailability(booking.getItem());
        validateNoOverlappingBookings(booking);
        validateItemNotOwnedByUser(booking);
    }

    public void validateBookingAccess(Booking booking, Long userId) {
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        boolean isBooker = booking.getBooker().getId().equals(userId);

        if (!isOwner && !isBooker) {
            throw new AccessDeniedException("Доступ к бронированию запрещен");
        }
    }

    public void validateOwnerRights(Booking booking, Long ownerId) {
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Только владелец может подтверждать бронирование");
        }
    }

    public void validateBookerRights(Booking booking, Long userId) {
        if (!booking.getBooker().getId().equals(userId)) {
            throw new AccessDeniedException("Только автор бронирования может отменить его");
        }
    }

    public void validateBookingStatusForApproval(Booking booking) {
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }
    }

    public void validateBookingStatusForCancellation(Booking booking) {
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Можно отменять только бронирования в статусе WAITING");
        }
    }

    public void validateUserExists(Long userId) {
        userService.getUserById(userId);
    }

    public void validateItemExists(Long itemId) {
        itemService.getItemById(itemId);
    }

    public void validateItemAvailability(Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
    }

    public void validateNoOverlappingBookings(Booking booking) {
        List<Booking> overlappingBookings = bookingRepository.findByItemIdAndStatus(
                booking.getItem().getId(), BookingStatus.APPROVED);

        for (Booking existingBooking : overlappingBookings) {
            if (isOverlapping(booking, existingBooking)) {
                throw new ValidationException("Вещь уже забронирована на указанные даты");
            }
        }
    }

    public boolean isOverlapping(Booking newBooking, Booking existingBooking) {
        return newBooking.getStart().isBefore(existingBooking.getEnd()) &&
                newBooking.getEnd().isAfter(existingBooking.getStart());
    }

    private void validateItemNotOwnedByUser(Booking booking) {
        if (booking.getItem().getOwner().getId().equals(booking.getBooker().getId())) {
            throw new ValidationException("Нельзя бронировать свою же вещь");
        }
    }
}

