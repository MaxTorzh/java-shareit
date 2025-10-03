package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
	@NotNull
	@FutureOrPresent(message = "Дата начала бронирования должна быть в будущем")
	private LocalDateTime start;

	@NotNull
	@Future(message = "Дата окончания бронирования должна быть в будущем")
	private LocalDateTime end;

	private Long itemId;
}
