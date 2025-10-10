package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(source = "requester.id", target = "requesterId")
    @Mapping(source = "requester", target = "requester")
    ItemRequestDto toDto(ItemRequest itemRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requester", source = "requester")
    @Mapping(target = "createdTime", ignore = true)
    ItemRequest toEntity(ItemRequestRequestDto requestDto, User requester);
}
