package ru.practicum.shareit.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.booking.validator.BookingValidator;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class BookingValidatorTest {

    @Autowired
    private TestEntityManager entityManager;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private BookingRepository bookingRepository;

    private BookingValidator bookingValidator;

    @BeforeEach
    void setUp() {
        bookingValidator = new BookingValidator(bookingRepository, userService, itemService);
    }

    @Test
    void validateBookingStatusForApproval_shouldThrowWhenNotWaiting() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.APPROVED);

        assertThrows(ValidationException.class, () ->
                bookingValidator.validateBookingStatusForApproval(booking));
    }

    @Test
    void validateBookingStatusForCancellation_shouldThrowWhenNotWaiting() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.APPROVED);

        assertThrows(ValidationException.class, () ->
                bookingValidator.validateBookingStatusForCancellation(booking));
    }

    @Test
    void validateBookingStatusForApproval_shouldNotThrowWhenWaiting() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.WAITING);

        bookingValidator.validateBookingStatusForApproval(booking);
    }

    @Test
    void validateBookingStatusForCancellation_shouldNotThrowWhenWaiting() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.WAITING);

        bookingValidator.validateBookingStatusForCancellation(booking);
    }
}


