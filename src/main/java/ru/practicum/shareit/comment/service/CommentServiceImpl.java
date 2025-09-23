package ru.practicum.shareit.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.comment.validator.CommentValidator;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentValidator commentValidator;
    private final ItemService itemService;
    private final UserService userService;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(Long itemId, CommentDto dto, Long authorId) {
        Item item = itemService.getItemById(itemId);
        User author = userService.getUserById(authorId);
        commentValidator.validateCommentCreation(item, author);
        Comment comment = commentMapper.toEntity(dto, item, author);
        comment.setCreated(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDto(savedComment);
    }

    @Override
    public Page<CommentDto> getCommentsByItemId(Long itemId, Pageable pageable) {
        Page<Comment> commentsPage = commentRepository.findByItemId(itemId, pageable);
        List<CommentDto> commentDtos = commentMapper.toDtoList(commentsPage.getContent());
        return new PageImpl<>(commentDtos, pageable, commentsPage.getTotalElements());
    }

    @Override
    public Page<CommentDto> getCommentsByItemIds(List<Long> itemIds, Pageable pageable) {
        Page<Comment> commentsPage = commentRepository.findByItemIdIn(itemIds, pageable);
        List<CommentDto> commentDtos = commentMapper.toDtoList(commentsPage.getContent());
        return new PageImpl<>(commentDtos, pageable, commentsPage.getTotalElements());
    }
}

