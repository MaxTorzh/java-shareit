package ru.practicum.shareit.booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.status.BookingState;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	/**
	 * Получение списка бронирований для пользователя с возможностью фильтрации по статусу.
	 *
	 * @param userId идентификатор пользователя, для которого запрашиваются бронирования
	 * @param stateParam параметр статуса бронирования (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED)
	 * @param from индекс первого элемента для пагинации (начиная с 0)
	 * @param size количество элементов для пагинации (больше 0)
	 * @return список бронирований пользователя в формате ResponseEntity
	 * @throws IllegalArgumentException если указан неизвестный статус бронирования
	 */
	@GetMapping
	public ResponseEntity<Object> getUserBookings(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(name = "state", defaultValue = "ALL") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getUserBookings(userId, state, from, size);
	}

	/**
	 * Создание нового бронирования вещи.
	 *
	 * @param userId идентификатор пользователя, создающего бронирование
	 * @param requestDto данные бронирования (идентификатор вещи, даты начала и окончания)
	 * @return созданное бронирование в формате ResponseEntity
	 */
	@PostMapping
	public ResponseEntity<Object> createBooking(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestBody @Valid BookItemRequestDto requestDto) {

		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.createBooking(userId, requestDto);
	}

	/**
	 * Получение информации о конкретном бронировании по его идентификатору.
	 *
	 * @param userId идентификатор пользователя, запрашивающего информацию
	 * @param bookingId идентификатор бронирования
	 * @return информация о бронировании в формате ResponseEntity
	 */
	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBookingById(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@PathVariable Long bookingId) {

		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBookingById(userId, bookingId);
	}

	/**
	 * Подтверждение или отклонение запроса на бронирование со стороны владельца вещи.
	 *
	 * @param userId идентификатор владельца вещи
	 * @param bookingId идентификатор бронирования
	 * @param approved true для подтверждения, false для отклонения
	 * @return обновленная информация о бронировании в формате ResponseEntity
	 */
	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approveBooking(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@PathVariable Long bookingId,
			@RequestParam Boolean approved) {

		log.info("Approve booking {}, userId={}, approved={}", bookingId, userId, approved);
		return bookingClient.approveBooking(userId, bookingId, approved);
	}

	/**
	 * Получение списка бронирований вещей, принадлежащих пользователю, с фильтрацией по статусу.
	 *
	 * @param userId идентификатор владельца вещей
	 * @param stateParam параметр статуса бронирования (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED)
	 * @param from индекс первого элемента для пагинации (начиная с 0)
	 * @param size количество элементов для пагинации (больше 0)
	 * @return список бронирований вещей пользователя в формате ResponseEntity
	 * @throws IllegalArgumentException если указан неизвестный статус бронирования
	 */
	@GetMapping("/owner")
	public ResponseEntity<Object> getOwnerBookings(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(name = "state", defaultValue = "ALL") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get owner bookings with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getOwnerBookings(userId, state, from, size);
	}

	/**
	 * Отмена бронирования.
	 *
	 * @param userId идентификатор пользователя, отменяющего бронирование
	 * @param bookingId идентификатор бронирования для отмены
	 * @return обновленная информация о бронировании в формате ResponseEntity
	 */
	@DeleteMapping("/{bookingId}")
	public ResponseEntity<Object> cancelBooking(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@PathVariable Long bookingId) {

		log.info("Cancel booking {}, userId={}", bookingId, userId);
		return bookingClient.cancelBooking(userId, bookingId);
	}
}
