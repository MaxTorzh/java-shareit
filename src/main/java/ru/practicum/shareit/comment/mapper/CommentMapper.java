package ru.practicum.shareit.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.name", target = "authorName")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", source = "item")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "created", ignore = true)
    Comment toEntity(CommentDto commentDto, ru.practicum.shareit.item.model.Item item,
                     ru.practicum.shareit.user.model.User author);

    List<CommentDto> toDtoList(List<Comment> comments);
}
