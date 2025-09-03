package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private Long id;
    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;
    @NotNull(message = "ID запрашивающего обязателен")
    private Long requesterId;
    @NotNull(message = "Дата создания обязательна")
    private LocalDateTime createdTime;
}
