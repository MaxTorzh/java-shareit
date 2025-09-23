package ru.practicum.shareit.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long itemId, CommentDto dto, Long authorId);

    Page<CommentDto> getCommentsByItemId(Long itemId, Pageable pageable);

    Page<CommentDto> getCommentsByItemIds(List<Long> itemIds, Pageable pageable);
}
