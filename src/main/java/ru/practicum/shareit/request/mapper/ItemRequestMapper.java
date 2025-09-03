package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

public class ItemRequestMapper {
    private static ItemRequestDto toDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(itemRequest.getId());
        dto.setDescription(itemRequest.getDescription());
        Optional.ofNullable(itemRequest.getRequester())
                .ifPresent(requester -> dto.setRequesterId(requester.getId()));
        dto.setCreatedTime(itemRequest.getCreatedTime());
        return dto;
    }

    private static ItemRequest toItemRequest(ItemRequestDto dto, User requester) {
        if (dto == null) {
            return null;
        }

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(dto.getId());
        itemRequest.setDescription(dto.getDescription());
        itemRequest.setRequester(requester);
        itemRequest.setCreatedTime(dto.getCreatedTime());
        return itemRequest;
    }
}
