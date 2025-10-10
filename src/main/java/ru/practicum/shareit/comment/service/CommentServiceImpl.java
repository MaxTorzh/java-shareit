package ru.practicum.shareit.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public Comment createComment(Long itemId, Comment comment, Long authorId) {
        Item item = itemService.getItemById(itemId);
        User author = userService.getUserById(authorId);
        commentValidator.validateCommentCreation(item, author);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Override
    public Page<CommentDto> getCommentsByItemId(Long itemId, Pageable pageable) {
        return commentRepository.findByItemId(itemId, pageable)
                .map(commentMapper::toDto);
    }
}

