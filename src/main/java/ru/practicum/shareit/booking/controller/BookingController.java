package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;

    /**
     * Создание нового бронирования.
     *
     * @param bookingDto данные нового бронирования
     * @param bookerId ID арендатора
     * @return созданное бронирование
     */
    @PostMapping
    public BookingDto createBooking(@Valid @RequestBody BookingDto bookingDto,
                                    @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        log.info("Получен запрос на создание бронирования от пользователя с ID: {}", bookerId);
        return BookingMapper.toDto(
                bookingService.createBooking(BookingMapper.toBooking(bookingDto,
                                itemService.getItemById(bookingDto.getItemId()),
                                userService.getUserById(bookerId))
                )
        );
    }

    /**
     * Подтверждение или отклонение бронирования владельцем.
     *
     * @param bookingId ID бронирования
     * @param approved флаг подтверждения
     * @param ownerId ID владельца предмета
     * @return обновленное бронирование
     */
    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@Valid @PathVariable Long bookingId,
                                     @RequestParam Boolean approved,
                                     @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Получен запрос на {} бронирования с ID: {} от владельца с ID: {}",
                approved ? "подтверждение" : "отклонение", bookingId, ownerId);
        return BookingMapper.toDto(bookingService.approveBooking(bookingId, approved, ownerId));
    }

    /**
     * Получение данных о бронировании.
     *
     * @param bookingId ID бронирования
     * @param userId ID пользователя, запрашивающего данные
     * @return данные бронирования
     */
    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable Long bookingId,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на получение данных бронирования с ID: {} от пользователя с ID: {}", bookingId, userId);
        return BookingMapper.toDto(bookingService.getBookingByIdWithAccessCheck(bookingId, userId));
    }

    /**
     * Получение списка бронирований пользователя.
     *
     * @param userId ID пользователя
     * @return список бронирований
     */
    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на получение списка бронирований пользователя с ID: {}", userId);
        return bookingService.getUserBookings(userId).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение списка бронирований как владелец предмета.
     *
     * @param ownerId ID владельца предметов
     * @return список бронирований
     */
    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Получен запрос на получение списка бронирований как владелец с ID: {}", ownerId);
        return bookingService.getOwnerBookings(ownerId).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Отмена бронирования.
     *
     * @param bookingId ID бронирования
     * @param userId ID пользователя, отменяющего бронирование
     */
    @DeleteMapping("/{bookingId}")
    public void cancelBooking(@PathVariable Long bookingId,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на отмену бронирования с ID: {} от пользователя с ID: {}", bookingId, userId);
        bookingService.cancelBooking(bookingId, userId);
    }
}
