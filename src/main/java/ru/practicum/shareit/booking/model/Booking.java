package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Дата начала бронирования обязательна")
    @Future(message = "Дата начала должна быть в будущем")
    private LocalDateTime start;

    @Column(name = "end_date")
    @NotNull(message = "Дата окончания бронирования обязательна")
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDateTime end;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @NotNull(message = "Предмет обязателен")
    private Item item;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id", nullable = false)
    @NotNull(message = "Заказчик обязателен")
    private User booker;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Статус бронирования обязателен")
    private BookingStatus status;
}
