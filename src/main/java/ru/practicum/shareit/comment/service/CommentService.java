package ru.practicum.shareit.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;

public interface CommentService {

    Comment createComment(Long itemId, Comment comment, Long authorId);

    Page<CommentDto> getCommentsByItemId(Long itemId, Pageable pageable);
}
