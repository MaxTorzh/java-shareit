package ru.practicum.shareit.booking.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
public class Booking {
    private Long id;
    @NotNull(message = "Дата начала бронирования обязательна")
    @Future(message = "Дата начала должна быть в будущем")
    private LocalDateTime start;
    @NotNull(message = "Дата окончания бронирования обязательна")
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDateTime end;
    @NotNull(message = "Предмет обязателен")
    private Item item;
    @NotNull(message = "Заказчик обязателен")
    private User booker;
    @NotNull(message = "Статус бронирования обязателен")
    private BookingStatus status;
}
