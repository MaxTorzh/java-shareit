package ru.practicum.shareit.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.item.id = :itemId ORDER BY c.created DESC")
    Page<Comment> findByItemId(Long itemId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.item.id IN :itemIds ORDER BY c.created DESC")
    Page<Comment> findByItemIdIn(List<Long> itemIds, Pageable pageable);
}
