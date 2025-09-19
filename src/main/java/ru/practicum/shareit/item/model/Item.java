package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Название предмета не может быть пустым")
    private String name;

    @Column(name = "description", nullable = false)
    @NotBlank(message = "Описание предмета не может быть пустым")
    private String description;

    @Column(name = "is_available", nullable = false)
    @NotNull(message = "Статус доступности обязателен")
    private Boolean available;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @NotNull(message = "Владелец обязателен")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ItemRequest request;
}
