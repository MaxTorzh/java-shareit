package ru.practicum.shareit.request.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private Long id;
    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;
    @NotNull(message = "Запрашивающий обязателен")
    private User requester;
    @NotNull(message = "Дата создания обязательна")
    private LocalDateTime createdTime;
}
