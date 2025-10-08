package ru.practicum.shareit.comment.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentValidator {
    private final BookingRepository bookingRepository;

    public void validateCommentCreation(Item item, User author) {
        validateUserRentedItem(item, author);
        validateRentalFinished(item, author);
    }

    private void validateUserRentedItem(Item item, User author) {
        List<Booking> userBookings = bookingRepository.findByBookerIdAndItemIdAndStatus(
                author.getId(),
                item.getId(),
                BookingStatus.APPROVED
        );
        if (userBookings.isEmpty()) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду");
        }
    }

    private void validateRentalFinished(Item item, User author) {
        List<Booking> finishedBookings = bookingRepository.findFinishedBookingsByUserAndItem(
                author.getId(),
                item.getId(),
                BookingStatus.APPROVED,
                LocalDateTime.now()
        );
        if (finishedBookings.isEmpty()) {
            throw new ValidationException("Можно оставлять комментарий только после окончания аренды");
        }
    }
}
