package ru.practicum.shareit.comment.service;

import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long itemId, CommentDto dto, Long authorId);

    List<CommentDto> getCommentsByItemId(Long itemId);

    List<CommentDto> getCommentsByItemIds(List<Long> itemIds);
}
