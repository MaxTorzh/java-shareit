package ru.practicum.shareit.comment.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentValidatorTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private CommentValidator commentValidator;

    private Item item;
    private User user;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);

        user = new User();
        user.setId(1L);
    }

    /**
     * Тест валидации, когда пользователь не арендовал предмет.
     * Проверяет выброс исключения ValidationException.
     */
    @Test
    void validateCommentCreation_shouldThrowExceptionWhenUserNotRentedItem() {
        // Нет бронирований у пользователя
        when(bookingRepository.findByBookerIdAndItemIdAndStatus(
                eq(user.getId()), eq(item.getId()), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of()); // Пустой список

        // Проверяем, что выбрасывается исключение
        ValidationException exception = assertThrows(ValidationException.class,
                () -> commentValidator.validateCommentCreation(item, user));

        assertEquals("Пользователь не брал эту вещь в аренду", exception.getMessage());

        // Проверяем, что второй метод не вызывается
        verify(bookingRepository).findByBookerIdAndItemIdAndStatus(
                user.getId(), item.getId(), BookingStatus.APPROVED);
        verify(bookingRepository, never()).findFinishedBookingsByUserAndItem(
                anyLong(), anyLong(), any(BookingStatus.class), any(LocalDateTime.class));
    }

    /**
     * Тест валидации с несколькими активными бронированиями.
     * Проверяет корректную работу с несколькими бронированиями.
     */
    @Test
    void validateCommentCreation_shouldPassWithMultipleBookings() {
        // Несколько бронирований
        Booking booking1 = new Booking();
        booking1.setId(1L);

        Booking booking2 = new Booking();
        booking2.setId(2L);

        when(bookingRepository.findByBookerIdAndItemIdAndStatus(
                eq(user.getId()), eq(item.getId()), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(booking1, booking2));

        when(bookingRepository.findFinishedBookingsByUserAndItem(
                eq(user.getId()), eq(item.getId()), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking1));

        // Выполняем проверку - исключений быть не должно
        assertDoesNotThrow(() -> commentValidator.validateCommentCreation(item, user));
    }

    /**
     * Тест валидации с несколькими завершенными бронированиями.
     * Проверяет корректную работу с несколькими завершенными бронированиями.
     */
    @Test
    void validateCommentCreation_shouldPassWithMultipleFinishedBookings() {
        Booking booking1 = new Booking();
        booking1.setId(1L);

        Booking booking2 = new Booking();
        booking2.setId(2L);

        when(bookingRepository.findByBookerIdAndItemIdAndStatus(
                eq(user.getId()), eq(item.getId()), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(booking1));

        when(bookingRepository.findFinishedBookingsByUserAndItem(
                eq(user.getId()), eq(item.getId()), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking1, booking2));

        // Выполняем проверку - исключений быть не должно
        assertDoesNotThrow(() -> commentValidator.validateCommentCreation(item, user));
    }

    /**
     * Тест валидации, когда нет ни одного завершенного бронирования.
     * Проверяет случай, когда все бронирования еще активны.
     */
    @Test
    void validateCommentCreation_shouldFailWhenNoFinishedBookings() {
        Booking booking = new Booking();
        booking.setId(1L);

        when(bookingRepository.findByBookerIdAndItemIdAndStatus(
                eq(user.getId()), eq(item.getId()), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(booking));

        when(bookingRepository.findFinishedBookingsByUserAndItem(
                eq(user.getId()), eq(item.getId()), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of()); // Нет завершенных бронирований

        ValidationException exception = assertThrows(ValidationException.class,
                () -> commentValidator.validateCommentCreation(item, user));

        assertEquals("Можно оставлять комментарий только после окончания аренды", exception.getMessage());
    }

    /**
     * Тест валидации с разными статусами бронирований.
     * Проверяет, что учитываются только APPROVED бронирования.
     */
    @Test
    void validateCommentCreation_shouldCheckOnlyApprovedBookings() {
        // Только отклоненные бронирования
        when(bookingRepository.findByBookerIdAndItemIdAndStatus(
                eq(user.getId()), eq(item.getId()), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of()); // Нет APPROVED бронирований

        ValidationException exception = assertThrows(ValidationException.class,
                () -> commentValidator.validateCommentCreation(item, user));

        assertEquals("Пользователь не брал эту вещь в аренду", exception.getMessage());
    }

    /**
     * Тест метода validateUserRentedItem отдельно.
     * Проверяет работу приватного метода через публичный.
     */
    @Test
    void validateUserRentedItem_shouldThrowExceptionWhenNoBookings() {
        when(bookingRepository.findByBookerIdAndItemIdAndStatus(
                eq(user.getId()), eq(item.getId()), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> commentValidator.validateCommentCreation(item, user));

        assertEquals("Пользователь не брал эту вещь в аренду", exception.getMessage());
    }

    /**
     * Тест метода validateRentalFinished отдельно.
     * Проверяет работу приватного метода через публичный.
     */
    @Test
    void validateRentalFinished_shouldThrowExceptionWhenNoFinishedBookings() {
        Booking booking = new Booking();
        booking.setId(1L);

        when(bookingRepository.findByBookerIdAndItemIdAndStatus(
                eq(user.getId()), eq(item.getId()), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(booking));

        when(bookingRepository.findFinishedBookingsByUserAndItem(
                eq(user.getId()), eq(item.getId()), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> commentValidator.validateCommentCreation(item, user));

        assertEquals("Можно оставлять комментарий только после окончания аренды", exception.getMessage());
    }
}

