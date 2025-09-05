package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    @NotNull(message = "Дата начала бронирования обязательна")
    @FutureOrPresent(message = "Дата начала должна быть в будущем или настоящем")
    private LocalDateTime startTime;
    @NotNull(message = "Дата окончания бронирования обязательна")
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDateTime endTime;
    @NotNull(message = "ID предмета обязателен")
    private Long itemId;
    private String status;
}
