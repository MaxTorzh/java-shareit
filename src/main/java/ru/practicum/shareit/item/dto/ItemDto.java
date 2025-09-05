package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank(message = "Название предмета не может быть пустым")
    private String name;
    @NotBlank(message = "Описание предмета не может быть пустым")
    private String description;
    @JsonProperty("available")
    private Boolean isAvailable;
    private Long ownerId;
    private Long requestId;
}
