package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingListDto;
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
    public BookingDto createBooking(
            @RequestBody BookItemRequestDto bookingRequestDto,
            @RequestHeader("X-Sharer-User-Id") long bookerId) {

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
    public BookingDto approveBooking(
            @PathVariable long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader("X-Sharer-User-Id") long ownerId) {

        log.info("Получен запрос на {} бронирования с ID: {} от владельца с ID: {}",
                approved ? "подтверждение" : "отклонение", bookingId, ownerId);
        return bookingMapper.toDto(bookingService.approveBooking(bookingId, approved, ownerId));
    }

    /**
     * Получение данных о бронировании.
     */
    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(
            @PathVariable long bookingId,
            @RequestHeader("X-Sharer-User-Id") long userId) {

        log.info("Получен запрос на получение данных бронирования с ID: {} от пользователя с ID: {}", bookingId, userId);
        return bookingMapper.toDto(bookingService.getBookingByIdWithAccessCheck(bookingId, userId));
    }

    /**
     * Получение списка бронирований пользователя.
     */
    @GetMapping
    public List<BookingDto> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получен запрос на получение списка бронирований пользователя с ID: {}, state: {}", userId, state);
        Pageable pageable = PageRequest.of(from / size, size);
        return bookingService.getUserBookings(userId, state, pageable).stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение списка бронирований как владелец предмета.
     */
    @GetMapping("/owner")
    public List<BookingListDto> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получен запрос на получение списка бронирований как владелец с ID: {}, state: {}", ownerId, state);
        Pageable pageable = PageRequest.of(from / size, size);
        return bookingService.getOwnerBookings(ownerId, state, pageable).stream()
                .map(bookingMapper::toListDto)
                .collect(Collectors.toList());
    }

    /**
     * Отмена бронирования.
     */
    @DeleteMapping("/{bookingId}")
    public void cancelBooking(
            @PathVariable long bookingId,
            @RequestHeader("X-Sharer-User-Id") long userId) {

        log.info("Получен запрос на отмену бронирования с ID: {} от пользователя с ID: {}", bookingId, userId);
        bookingService.cancelBooking(bookingId, userId);
    }
}

