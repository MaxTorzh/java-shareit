package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.booking.validator.BookingValidator;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Интеграционные тесты для сервиса бронирований {@link BookingServiceImpl}.
 * Тестирует все основные функции сервиса бронирований:
 * - Создание бронирования
 * - Получение бронирования по ID
 * - Подтверждение/отклонение бронирования
 * - Получение списков бронирований пользователя и владельца
 * - Отмена бронирования
 *
 * Тесты используют реальную базу данных в памяти (H2) через TestEntityManager
 * и моки внешних сервисов (UserService, ItemService) для изоляции тестируемого функционала.</p>
 *
 * <p>Класс использует аннотацию {@link DirtiesContext} для очистки контекста
 * после каждого теста, обеспечивая независимость тестов друг от друга.
 */
@DataJpaTest
@Import({BookingServiceImpl.class, BookingValidator.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Создает тестовые сущности в базе данных:
     * - Пользователя-владельца предмета
     * - Пользователя-бронирующего
     * - Предмет для бронирования
     * - Настраивает моки внешних сервисов
     *
     * Моки настроены для возврата созданных сущностей при запросе по ID
     * и выбрасывания исключений при запросе несуществующих сущностей.
     */
    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Item Owner");
        owner.setEmail("owner@test.com");
        owner = entityManager.persistAndFlush(owner);

        booker = new User();
        booker.setName("Item Booker");
        booker.setEmail("booker@test.com");
        booker = entityManager.persistAndFlush(booker);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = entityManager.persistAndFlush(item);

        when(userService.getUserById(owner.getId())).thenReturn(owner);
        when(userService.getUserById(booker.getId())).thenReturn(booker);
        when(userService.getUserById(999L)).thenThrow(new NotFoundException("User not found"));

        when(itemService.getItemById(item.getId())).thenReturn(item);
        when(itemService.getItemById(999L)).thenThrow(new NotFoundException("Item not found"));

        booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
    }

    /**
     * Тест создания нового бронирования.
     * Проверяет, что бронирование успешно создается в базе данных
     * с правильными данными и статусом WAITING по умолчанию.
     */
    @Test
    void createBooking_shouldCreateAndReturnBooking() {
        Booking createdBooking = bookingService.createBooking(booking);

        assertNotNull(createdBooking.getId());
        assertEquals(booking.getStart(), createdBooking.getStart());
        assertEquals(booking.getEnd(), createdBooking.getEnd());
        assertEquals(item.getId(), createdBooking.getItem().getId());
        assertEquals(booker.getId(), createdBooking.getBooker().getId());
        assertEquals(BookingStatus.WAITING, createdBooking.getStatus());
    }

    /**
     * Тест получения бронирования по ID.
     * Проверяет, что существующее бронирование успешно возвращается
     * с правильными данными.
     */
    @Test
    void getBookingById_shouldReturnBookingWhenExists() {
        Booking savedBooking = bookingRepository.save(booking);

        Booking foundBooking = bookingService.getBookingById(savedBooking.getId());

        assertNotNull(foundBooking);
        assertEquals(savedBooking.getId(), foundBooking.getId());
        assertEquals(savedBooking.getStart(), foundBooking.getStart());
        assertEquals(savedBooking.getEnd(), foundBooking.getEnd());
    }

    /**
     * Тест получения несуществующего бронирования по ID.
     * Проверяет, что при попытке получить несуществующее бронирование
     * выбрасывается исключение {@link NotFoundException}.
     */
    @Test
    void getBookingById_shouldThrowNotFoundExceptionWhenNotExists() {
        Long nonExistingBookingId = 999L;

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(nonExistingBookingId));
    }

    /**
     * Тест подтверждения бронирования владельцем.
     * Проверяет, что бронирование успешно подтверждается
     * и получает статус APPROVED.
     */
    @Test
    void approveBooking_shouldApproveBooking() {
        Booking savedBooking = bookingRepository.save(booking);

        Booking approvedBooking = bookingService.approveBooking(savedBooking.getId(), true, owner.getId());

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    /**
     * Тест отклонения бронирования владельцем.
     * Проверяет, что бронирование успешно отклоняется
     * и получает статус REJECTED.
     */
    @Test
    void approveBooking_shouldRejectBooking() {
        Booking savedBooking = bookingRepository.save(booking);

        Booking rejectedBooking = bookingService.approveBooking(savedBooking.getId(), false, owner.getId());

        assertEquals(BookingStatus.REJECTED, rejectedBooking.getStatus());
    }

    /**
     * Тест получения списка бронирований пользователя.
     * Проверяет, что список бронирований пользователя успешно возвращается
     * и содержит созданное бронирование.
     */
    @Test
    void getUserBookings_shouldReturnUserBookings() {
        Booking savedBooking = bookingRepository.save(booking);
        Pageable pageable = PageRequest.of(0, 10);

        var userBookings = bookingService.getUserBookings(booker.getId(), "ALL", pageable);

        assertEquals(1, userBookings.getTotalElements());
        assertEquals(savedBooking.getId(), userBookings.getContent().get(0).getId());
    }

    /**
     * Тест получения списка бронирований для владельца предметов.
     * Проверяет, что список бронирований владельца успешно возвращается
     * и содержит бронирования на его предметы.
     */
    @Test
    void getOwnerBookings_shouldReturnOwnerBookings() {
        Booking savedBooking = bookingRepository.save(booking);
        Pageable pageable = PageRequest.of(0, 10);

        var ownerBookings = bookingService.getOwnerBookings(owner.getId(), "ALL", pageable);

        assertEquals(1, ownerBookings.getTotalElements());
        assertEquals(savedBooking.getId(), ownerBookings.getContent().get(0).getId());
    }

    /**
     * Тест отмены бронирования.
     * Проверяет, что бронирование успешно отменяется пользователем
     * и получает статус CANCELLED в базе данных.
     */
    @Test
    void cancelBooking_shouldCancelBooking() {
        Booking savedBooking = bookingRepository.save(booking);

        bookingService.cancelBooking(savedBooking.getId(), booker.getId());

        Booking cancelledBooking = bookingRepository.findById(savedBooking.getId()).orElse(null);
        assertNotNull(cancelledBooking);
        assertEquals(BookingStatus.CANCELLED, cancelledBooking.getStatus());
    }
}
