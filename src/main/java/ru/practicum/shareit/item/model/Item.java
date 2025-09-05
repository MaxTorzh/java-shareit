package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

@Data
public class Item {
    private Long id;
    @NotBlank(message = "Название предмета не может быть пустым")
    private String name;
    @NotBlank(message = "Описание предмета не может быть пустым")
    private String description;
    @NotNull(message = "Статус доступности обязателен")
    private Boolean available;
    @NotNull(message = "Владелец обязателен")
    private User owner;
    private Long requestId;
}
