package ru.practicum.shareit.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.email", target = "email")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "userDto.name", target = "name")
    @Mapping(source = "userDto.email", target = "email")
    User toEntity(UserDto userDto);
}
