package ru.practicum.shareit.comment.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public static CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        if (comment.getItem() != null) {
            dto.setItemId(comment.getItem().getId());
        }
        if (comment.getAuthor() != null) {
            dto.setAuthor_id(comment.getAuthor().getId());
            dto.setAuthorName(comment.getAuthor().getName());
        }
        dto.setCreated(comment.getCreated());
        return dto;
    }

    public static Comment toComment(CommentDto dto, Item item, User author) {
        if (dto == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setId(dto.getId());
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    public static List<CommentDto> toDtoList(List<Comment> comments) {
        if (comments == null) {
            return Collections.emptyList();
        }
        return comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }
}
