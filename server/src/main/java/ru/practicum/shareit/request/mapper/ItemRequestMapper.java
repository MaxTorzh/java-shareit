package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(source = "requester.id", target = "requesterId")
    @Mapping(target = "requester", ignore = true)
    @Mapping(source = "createdTime", target = "created")
    @Mapping(target = "items", ignore = true)
    ItemRequestsDto toDto(ItemRequest itemRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requester", source = "requester")
    @Mapping(target = "createdTime", ignore = true)
    ItemRequest toEntity(ItemRequestRequestDto requestDto, User requester);
}
