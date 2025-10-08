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
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Slice тесты для сервиса бронирований {@link BookingServiceImpl}.
 * Тестируют функциональность сервиса с использованием реальной базы данных
 * и моков для внешних сервисов (UserService, ItemService).
 *
 * Используют @DataJpaTest для тестирования слоя работы с БД и @Import для
 * загрузки тестируемого сервиса и его зависимостей.
 *
 * Класс использует аннотацию {@link DirtiesContext} для очистки контекста
 * после каждого теста, обеспечивая независимость тестов друг от друга.
 */
@DataJpaTest
@Import({BookingServiceImpl.class, BookingValidator.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingServiceImplSliceTest {

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
     * Тест получения бронирования по ID с проверкой доступа.
     * Проверяет, что пользователь может получить бронирование, если он является
     * бронирующим или владельцем предмета.
     */
    @Test
    void getBookingByIdWithAccessCheck_shouldReturnBookingForBooker() {
        Booking savedBooking = bookingRepository.save(booking);

        Booking foundBooking = bookingService.getBookingByIdWithAccessCheck(savedBooking.getId(), booker.getId());

        assertNotNull(foundBooking);
        assertEquals(savedBooking.getId(), foundBooking.getId());
    }

    /**
     * Тест получения бронирования по ID с проверкой доступа для владельца.
     * Проверяет, что владелец предмета может получить бронирование.
     */
    @Test
    void getBookingByIdWithAccessCheck_shouldReturnBookingForOwner() {
        Booking savedBooking = bookingRepository.save(booking);

        Booking foundBooking = bookingService.getBookingByIdWithAccessCheck(savedBooking.getId(), owner.getId());

        assertNotNull(foundBooking);
        assertEquals(savedBooking.getId(), foundBooking.getId());
    }

    /**
     * Тест получения бронирования по ID с проверкой доступа для несуществующего бронирования.
     * Проверяет, что при попытке получить несуществующее бронирование выбрасывается исключение.
     */
    @Test
    void getBookingByIdWithAccessCheck_shouldThrowExceptionWhenBookingNotExists() {
        assertThrows(NotFoundException.class, () ->
                bookingService.getBookingByIdWithAccessCheck(999L, booker.getId()));
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
     * Тест попытки подтверждения бронирования не владельцем.
     * Проверяет, что подтверждение бронирования не владельцем предмета запрещено.
     */
    @Test
    void approveBooking_shouldThrowExceptionWhenNotOwner() {
        Booking savedBooking = bookingRepository.save(booking);

        assertThrows(AccessDeniedException.class, () ->
                bookingService.approveBooking(savedBooking.getId(), true, booker.getId()));
    }

    /**
     * Тест попытки подтверждения несуществующего бронирования.
     * Проверяет, что подтверждение несуществующего бронирования вызывает исключение.
     */
    @Test
    void approveBooking_shouldThrowExceptionWhenBookingNotExists() {
        assertThrows(NotFoundException.class, () ->
                bookingService.approveBooking(999L, true, owner.getId()));
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
     * Тест получения списка бронирований пользователя с пагинацией.
     * Проверяет, что пагинация работает корректно.
     */
    @Test
    void getUserBookings_shouldHandlePagination() {
        // Создаем несколько бронирований
        for (int i = 0; i < 5; i++) {
            Booking testBooking = new Booking();
            testBooking.setStart(LocalDateTime.now().plusDays(i + 1));
            testBooking.setEnd(LocalDateTime.now().plusDays(i + 2));
            testBooking.setItem(item);
            testBooking.setBooker(booker);
            testBooking.setStatus(BookingStatus.WAITING);
            bookingRepository.save(testBooking);
        }

        Pageable pageable = PageRequest.of(1, 2); // Вторая страница, 2 элемента на странице
        var userBookings = bookingService.getUserBookings(booker.getId(), "ALL", pageable);

        assertEquals(5, userBookings.getTotalElements());
        assertEquals(2, userBookings.getContent().size());
    }

    /**
     * Тест получения списка бронирований пользователя с текущими бронированиями.
     */
    @Test
    void getUserBookings_shouldReturnCurrentBookings() {
        // Создаем текущее бронирование
        Booking currentBooking = new Booking();
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        currentBooking.setItem(item);
        currentBooking.setBooker(booker);
        currentBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(currentBooking);

        Pageable pageable = PageRequest.of(0, 10);
        var userBookings = bookingService.getUserBookings(booker.getId(), "CURRENT", pageable);

        assertEquals(1, userBookings.getTotalElements());
        assertEquals(currentBooking.getId(), userBookings.getContent().get(0).getId());
    }

    /**
     * Тест получения списка бронирований пользователя с прошлыми бронированиями.
     */
    @Test
    void getUserBookings_shouldReturnPastBookings() {
        // Создаем прошедшее бронирование
        Booking pastBooking = new Booking();
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        Pageable pageable = PageRequest.of(0, 10);
        var userBookings = bookingService.getUserBookings(booker.getId(), "PAST", pageable);

        assertEquals(1, userBookings.getTotalElements());
        assertEquals(pastBooking.getId(), userBookings.getContent().get(0).getId());
    }

    /**
     * Тест получения списка бронирований пользователя с будущими бронированиями.
     */
    @Test
    void getUserBookings_shouldReturnFutureBookings() {
        Booking savedBooking = bookingRepository.save(booking);

        Pageable pageable = PageRequest.of(0, 10);
        var userBookings = bookingService.getUserBookings(booker.getId(), "FUTURE", pageable);

        assertEquals(1, userBookings.getTotalElements());
        assertEquals(savedBooking.getId(), userBookings.getContent().get(0).getId());
    }

    /**
     * Тест получения списка бронирований пользователя с ожидающими бронированиями.
     */
    @Test
    void getUserBookings_shouldReturnWaitingBookings() {
        Booking savedBooking = bookingRepository.save(booking);

        Pageable pageable = PageRequest.of(0, 10);
        var userBookings = bookingService.getUserBookings(booker.getId(), "WAITING", pageable);

        assertEquals(1, userBookings.getTotalElements());
        assertEquals(savedBooking.getId(), userBookings.getContent().get(0).getId());
    }

    /**
     * Тест получения списка бронирований пользователя с отклоненными бронированиями.
     */
    @Test
    void getUserBookings_shouldReturnRejectedBookings() {
        Booking rejectedBooking = new Booking();
        rejectedBooking.setStart(LocalDateTime.now().plusDays(1));
        rejectedBooking.setEnd(LocalDateTime.now().plusDays(2));
        rejectedBooking.setItem(item);
        rejectedBooking.setBooker(booker);
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(rejectedBooking);

        Pageable pageable = PageRequest.of(0, 10);
        var userBookings = bookingService.getUserBookings(booker.getId(), "REJECTED", pageable);

        assertEquals(1, userBookings.getTotalElements());
        assertEquals(rejectedBooking.getId(), userBookings.getContent().get(0).getId());
    }

    /**
     * Тест получения списка бронирований пользователя с отмененными бронированиями.
     */
    @Test
    void getUserBookings_shouldReturnCancelledBookings() {
        Booking cancelledBooking = new Booking();
        cancelledBooking.setStart(LocalDateTime.now().plusDays(1));
        cancelledBooking.setEnd(LocalDateTime.now().plusDays(2));
        cancelledBooking.setItem(item);
        cancelledBooking.setBooker(booker);
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(cancelledBooking);

        Pageable pageable = PageRequest.of(0, 10);
        var userBookings = bookingService.getUserBookings(booker.getId(), "ALL", pageable);

        assertEquals(1, userBookings.getTotalElements());
        assertEquals(cancelledBooking.getId(), userBookings.getContent().get(0).getId());
    }

    /**
     * Тест получения списка бронирований пользователя без бронирований.
     * Проверяет, что возвращается пустой список.
     */
    @Test
    void getUserBookings_shouldReturnEmptyListWhenNoBookings() {
        Pageable pageable = PageRequest.of(0, 10);
        var userBookings = bookingService.getUserBookings(booker.getId(), "ALL", pageable);

        assertEquals(0, userBookings.getTotalElements());
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
     * Тест получения списка бронирований владельца с различными статусами.
     */
    @Test
    void getOwnerBookings_shouldHandleDifferentStates() {
        Booking savedBooking = bookingRepository.save(booking);

        Pageable pageable = PageRequest.of(0, 10);

        // Тест разных состояний
        var allBookings = bookingService.getOwnerBookings(owner.getId(), "ALL", pageable);
        var waitingBookings = bookingService.getOwnerBookings(owner.getId(), "WAITING", pageable);

        assertEquals(1, allBookings.getTotalElements());
        assertEquals(1, waitingBookings.getTotalElements());
    }

    /**
     * Тест получения списка бронирований владельца с пагинацией.
     * Проверяет, что пагинация работает корректно для владельца.
     */
    @Test
    void getOwnerBookings_shouldHandlePagination() {
        // Создаем несколько бронирований
        for (int i = 0; i < 5; i++) {
            Booking testBooking = new Booking();
            testBooking.setStart(LocalDateTime.now().plusDays(i + 1));
            testBooking.setEnd(LocalDateTime.now().plusDays(i + 2));
            testBooking.setItem(item);
            testBooking.setBooker(booker);
            testBooking.setStatus(BookingStatus.WAITING);
            bookingRepository.save(testBooking);
        }

        Pageable pageable = PageRequest.of(0, 3); // Первая страница, 3 элемента на странице
        var ownerBookings = bookingService.getOwnerBookings(owner.getId(), "ALL", pageable);

        assertEquals(5, ownerBookings.getTotalElements());
        assertEquals(3, ownerBookings.getContent().size());
    }

    /**
     * Тест получения списка бронирований владельца с отклоненными бронированиями.
     */
    @Test
    void getOwnerBookings_shouldReturnRejectedBookings() {
        Booking rejectedBooking = new Booking();
        rejectedBooking.setStart(LocalDateTime.now().plusDays(1));
        rejectedBooking.setEnd(LocalDateTime.now().plusDays(2));
        rejectedBooking.setItem(item);
        rejectedBooking.setBooker(booker);
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(rejectedBooking);

        Pageable pageable = PageRequest.of(0, 10);
        var ownerBookings = bookingService.getOwnerBookings(owner.getId(), "REJECTED", pageable);

        assertEquals(1, ownerBookings.getTotalElements());
        assertEquals(rejectedBooking.getId(), ownerBookings.getContent().get(0).getId());
    }

    /**
     * Тест получения списка бронирований владельца без бронирований.
     * Проверяет, что возвращается пустой список.
     */
    @Test
    void getOwnerBookings_shouldReturnEmptyListWhenNoBookings() {
        User otherOwner = new User();
        otherOwner.setName("Other Owner");
        otherOwner.setEmail("otherowner@test.com");
        otherOwner = entityManager.persistAndFlush(otherOwner);

        Pageable pageable = PageRequest.of(0, 10);
        var ownerBookings = bookingService.getOwnerBookings(otherOwner.getId(), "ALL", pageable);

        assertEquals(0, ownerBookings.getTotalElements());
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

    /**
     * Тест попытки отмены бронирования другим пользователем.
     * Проверяет, что отмена бронирования возможна только бронирующим пользователем.
     */
    @Test
    void cancelBooking_shouldThrowExceptionWhenNotBooker() {
        Booking savedBooking = bookingRepository.save(booking);

        assertThrows(AccessDeniedException.class, () ->
                bookingService.cancelBooking(savedBooking.getId(), owner.getId()));
    }

    /**
     * Тест попытки отмены несуществующего бронирования.
     * Проверяет, что отмена несуществующего бронирования вызывает исключение.
     */
    @Test
    void cancelBooking_shouldThrowExceptionWhenBookingNotExists() {
        assertThrows(NotFoundException.class, () ->
                bookingService.cancelBooking(999L, booker.getId()));
    }
}

