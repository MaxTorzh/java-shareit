package ru.practicum.shareit.comment.service;

import lombok.RequiredArgsConstructor;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentValidator commentValidator;
    private final ItemService itemService;
    private final UserService userService;

    @Override
    @Transactional
    public CommentDto createComment(Long itemId, CommentDto dto, Long authorId) {
        Item item = itemService.getItemById(itemId);
        User author = userService.getUserById(authorId);
        commentValidator.validateCommentCreation(item, author);
        Comment comment = CommentMapper.toComment(dto, item, author);
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toDto(savedComment);
    }

    @Override
    public List<CommentDto> getCommentsByItemId(Long itemId) {
        List<Comment> comments = commentRepository.findByItemId(itemId);
        return CommentMapper.toDtoList(comments);
    }

    @Override
    public List<CommentDto> getCommentsByItemIds(List<Long> itemIds) {
        List<Comment> comments = commentRepository.findByItemIdIn(itemIds);
        return CommentMapper.toDtoList(comments);
    }
}
