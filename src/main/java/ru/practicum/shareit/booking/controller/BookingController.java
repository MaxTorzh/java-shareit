package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingListDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
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
    private final BookingMapper bookingMapper;

    /**
     * Создание нового бронирования.
     */
    @PostMapping
    public BookingDto createBooking(@Valid @RequestBody BookingRequestDto bookingRequestDto,
                                    @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        log.info("Получен запрос на создание бронирования от пользователя с ID: {}", bookerId);
        return bookingMapper.toDto(
                bookingService.createBooking(bookingMapper.toBooking(bookingRequestDto,
                                itemService.getItemById(bookingRequestDto.getItemId()),
                                userService.getUserById(bookerId))
                )
        );
    }

    /**
     * Подтверждение или отклонение бронирования владельцем.
     */
    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@Valid @PathVariable Long bookingId,
                                     @RequestParam Boolean approved,
                                     @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Получен запрос на {} бронирования с ID: {} от владельца с ID: {}",
                approved ? "подтверждение" : "отклонение", bookingId, ownerId);
        return bookingMapper.toDto(bookingService.approveBooking(bookingId, approved, ownerId));
    }

    /**
     * Получение данных о бронировании.
     */
    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable Long bookingId,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на получение данных бронирования с ID: {} от пользователя с ID: {}", bookingId, userId);
        return bookingMapper.toDto(bookingService.getBookingByIdWithAccessCheck(bookingId, userId));
    }

    /**
     * Получение списка бронирований пользователя.
     */
    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение списка бронирований пользователя с ID: {}", userId);
        Pageable pageable = PageRequest.of(from / size, size);
        return bookingService.getUserBookings(userId, pageable).stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение списка бронирований как владелец предмета.
     */
    @GetMapping("/owner")
    public List<BookingListDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение списка бронирований как владелец с ID: {}", ownerId);
        Pageable pageable = PageRequest.of(from / size, size);
        return bookingService.getOwnerBookings(ownerId, pageable).stream()
                .map(bookingMapper::toListDto)
                .collect(Collectors.toList());
    }

    /**
     * Отмена бронирования.
     */
    @DeleteMapping("/{bookingId}")
    public void cancelBooking(@PathVariable Long bookingId,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на отмену бронирования с ID: {} от пользователя с ID: {}", bookingId, userId);
        bookingService.cancelBooking(bookingId, userId);
    }
}
