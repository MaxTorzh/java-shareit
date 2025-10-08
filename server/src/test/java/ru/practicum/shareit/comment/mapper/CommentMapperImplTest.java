package ru.practicum.shareit.comment.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentMapperImplTest {

    @Autowired
    private CommentMapper commentMapper;

    private Comment comment;
    private CommentDto commentDto;
    private Item item;
    private User author;

    @BeforeEach
    void setUp() {
        // Создаем тестовые данные
        item = new Item();
        item.setId(1L);

        author = new User();
        author.setId(1L);
        author.setName("Test Author");

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);
        comment.setAuthor(author);

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Test comment");
        commentDto.setCreated(LocalDateTime.now());
        commentDto.setItemId(1L);
        commentDto.setAuthorId(1L);
        commentDto.setAuthorName("Test Author");
    }

    /**
     * Тест преобразования Comment в CommentDto.
     * Проверяет успешное преобразование всех полей.
     */
    @Test
    void toDto_shouldConvertCommentToCommentDto() {
        CommentDto result = commentMapper.toDto(comment);

        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getCreated(), result.getCreated());
        assertEquals(comment.getItem().getId(), result.getItemId());
        assertEquals(comment.getAuthor().getId(), result.getAuthorId());
        assertEquals(comment.getAuthor().getName(), result.getAuthorName());
    }

    /**
     * Тест преобразования Comment в CommentDto с null Comment.
     * Проверяет обработку null значения.
     */
    @Test
    void toDto_shouldReturnNullWhenCommentIsNull() {
        CommentDto result = commentMapper.toDto(null);

        assertNull(result);
    }

    /**
     * Тест преобразования Comment в CommentDto с null Item.
     * Проверяет обработку null значения в поле item.
     */
    @Test
    void toDto_shouldHandleNullItem() {
        comment.setItem(null);

        CommentDto result = commentMapper.toDto(comment);

        assertNotNull(result);
        assertNull(result.getItemId());
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getCreated(), result.getCreated());
    }

    /**
     * Тест преобразования Comment в CommentDto с null Author.
     * Проверяет обработку null значения в поле author.
     */
    @Test
    void toDto_shouldHandleNullAuthor() {
        comment.setAuthor(null);

        CommentDto result = commentMapper.toDto(comment);

        assertNotNull(result);
        assertNull(result.getAuthorId());
        assertNull(result.getAuthorName());
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getCreated(), result.getCreated());
    }

    /**
     * Тест преобразования Comment в CommentDto с null Item ID.
     * Проверяет обработку null значения в поле item.id.
     */
    @Test
    void toDto_shouldHandleNullItemId() {
        item.setId(null);
        comment.setItem(item);

        CommentDto result = commentMapper.toDto(comment);

        assertNotNull(result);
        assertNull(result.getItemId());
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getCreated(), result.getCreated());
    }

    /**
     * Тест преобразования Comment в CommentDto с null Author ID.
     * Проверяет обработку null значения в поле author.id.
     */
    @Test
    void toDto_shouldHandleNullAuthorId() {
        author.setId(null);
        comment.setAuthor(author);

        CommentDto result = commentMapper.toDto(comment);

        assertNotNull(result);
        assertNull(result.getAuthorId());
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getCreated(), result.getCreated());
    }

    /**
     * Тест преобразования Comment в CommentDto с null Author Name.
     * Проверяет обработку null значения в поле author.name.
     */
    @Test
    void toDto_shouldHandleNullAuthorName() {
        author.setName(null);
        comment.setAuthor(author);

        CommentDto result = commentMapper.toDto(comment);

        assertNotNull(result);
        assertNull(result.getAuthorName());
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getCreated(), result.getCreated());
    }

    /**
     * Тест преобразования CommentDto в Comment.
     * Проверяет успешное преобразование всех полей.
     */
    @Test
    void toEntity_shouldConvertCommentDtoToComment() {
        Comment result = commentMapper.toEntity(commentDto, item, author);

        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
        assertEquals(item, result.getItem());
        assertEquals(author, result.getAuthor());
        assertNull(result.getId()); // ID не устанавливается из DTO
        assertNull(result.getCreated()); // Created не устанавливается из DTO
    }

    /**
     * Тест преобразования CommentDto в Comment с null CommentDto.
     * Проверяет обработку null значения.
     */
    @Test
    void toEntity_shouldReturnNullWhenAllParametersAreNull() {
        Comment result = commentMapper.toEntity(null, null, null);

        assertNull(result);
    }

    /**
     * Тест преобразования CommentDto в Comment с null CommentDto, но с валидными item и author.
     * Проверяет обработку частично null значений.
     */
    @Test
    void toEntity_shouldCreateCommentWithNullDtoButValidItemAndAuthor() {
        Comment result = commentMapper.toEntity(null, item, author);

        assertNotNull(result);
        assertNull(result.getText());
        assertEquals(item, result.getItem());
        assertEquals(author, result.getAuthor());
    }

    /**
     * Тест преобразования CommentDto в Comment с валидным CommentDto, но null item и author.
     * Проверяет обработку частично null значений.
     */
    @Test
    void toEntity_shouldCreateCommentWithValidDtoButNullItemAndAuthor() {
        Comment result = commentMapper.toEntity(commentDto, null, null);

        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
        assertNull(result.getItem());
        assertNull(result.getAuthor());
    }

    /**
     * Тест преобразования CommentDto в Comment с пустым текстом.
     * Проверяет обработку пустых значений.
     */
    @Test
    void toEntity_shouldHandleEmptyText() {
        commentDto.setText("");

        Comment result = commentMapper.toEntity(commentDto, item, author);

        assertNotNull(result);
        assertEquals("", result.getText());
        assertEquals(item, result.getItem());
        assertEquals(author, result.getAuthor());
    }

    /**
     * Тест преобразования списка Comment в список CommentDto.
     * Проверяет успешное преобразование списка.
     */
    @Test
    void toDtoList_shouldConvertListOfComments() {
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);

        // Добавляем еще один комментарий
        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setText("Second comment");
        comment2.setCreated(LocalDateTime.now().plusDays(1));
        comment2.setItem(item);
        comment2.setAuthor(author);
        comments.add(comment2);

        List<CommentDto> result = commentMapper.toDtoList(comments);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(comment.getId(), result.get(0).getId());
        assertEquals(comment2.getId(), result.get(1).getId());
        assertEquals(comment.getText(), result.get(0).getText());
        assertEquals(comment2.getText(), result.get(1).getText());
    }

    /**
     * Тест преобразования пустого списка Comment в список CommentDto.
     * Проверяет обработку пустого списка.
     */
    @Test
    void toDtoList_shouldReturnEmptyListWhenInputIsEmpty() {
        List<Comment> comments = new ArrayList<>();

        List<CommentDto> result = commentMapper.toDtoList(comments);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Тест преобразования null списка Comment в список CommentDto.
     * Проверяет обработку null значения.
     */
    @Test
    void toDtoList_shouldReturnNullWhenInputIsNull() {
        List<CommentDto> result = commentMapper.toDtoList(null);

        assertNull(result);
    }

    /**
     * Тест преобразования списка Comment с null элементами.
     * Проверяет обработку null элементов в списке.
     */
    @Test
    void toDtoList_shouldHandleNullElementsInList() {
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        comments.add(null);
        comments.add(comment); // Добавляем еще один валидный элемент

        List<CommentDto> result = commentMapper.toDtoList(comments);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertNotNull(result.get(0));
        assertNull(result.get(1)); // Null элемент должен остаться null
        assertNotNull(result.get(2));
    }

    /**
     * Тест преобразования Comment с пустыми значениями.
     * Проверяет обработку граничных условий.
     */
    @Test
    void toDto_shouldHandleCommentWithEmptyValues() {
        Comment emptyComment = new Comment();
        emptyComment.setId(null);
        emptyComment.setText("");
        emptyComment.setCreated(null);
        emptyComment.setItem(null);
        emptyComment.setAuthor(null);

        CommentDto result = commentMapper.toDto(emptyComment);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("", result.getText());
        assertNull(result.getCreated());
        assertNull(result.getItemId());
        assertNull(result.getAuthorId());
        assertNull(result.getAuthorName());
    }

    /**
     * Тест преобразования CommentDto с пустыми значениями.
     * Проверяет обработку граничных условий.
     */
    @Test
    void toEntity_shouldHandleCommentDtoWithEmptyValues() {
        CommentDto emptyDto = new CommentDto();
        emptyDto.setId(null);
        emptyDto.setText("");
        emptyDto.setCreated(null);
        emptyDto.setItemId(null);
        emptyDto.setAuthorId(null);
        emptyDto.setAuthorName(null);

        Comment result = commentMapper.toEntity(emptyDto, item, author);

        assertNotNull(result);
        assertEquals("", result.getText());
        assertEquals(item, result.getItem());
        assertEquals(author, result.getAuthor());
        assertNull(result.getId());
        assertNull(result.getCreated());
    }

    /**
     * Тест производительности преобразования большого списка.
     * Проверяет обработку большого объема данных.
     */
    @Test
    void toDtoList_shouldHandleLargeList() {
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Comment c = new Comment();
            c.setId((long) i);
            c.setText("Comment " + i);
            c.setCreated(LocalDateTime.now().plusMinutes(i));
            c.setItem(item);
            c.setAuthor(author);
            comments.add(c);
        }

        List<CommentDto> result = commentMapper.toDtoList(comments);

        assertNotNull(result);
        assertEquals(1000, result.size());
        assertEquals(0L, result.get(0).getId());
        assertEquals(999L, result.get(999).getId());
    }
}

