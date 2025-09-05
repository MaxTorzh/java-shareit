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

    @PostMapping
    public BookingDto createBooking(@Valid @RequestBody BookingDto bookingDto,
                                    @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        return BookingMapper.toDto(
                bookingService.createBooking(BookingMapper.toBooking(bookingDto,
                                itemService.getItemById(bookingDto.getItemId()),
                                userService.getUserById(bookerId))
                )
        );
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@Valid @PathVariable Long bookingId,
                                     @RequestParam Boolean approved,
                                     @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return BookingMapper.toDto(bookingService.approveBooking(bookingId, approved, ownerId));
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable Long bookingId,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        return BookingMapper.toDto(bookingService.getBookingByIdWithAccessCheck(bookingId, userId));
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getUserBookings(userId).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return bookingService.getOwnerBookings(ownerId).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{bookingId}")
    public void cancelBooking(@PathVariable Long bookingId,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        bookingService.cancelBooking(bookingId, userId);
    }
}
