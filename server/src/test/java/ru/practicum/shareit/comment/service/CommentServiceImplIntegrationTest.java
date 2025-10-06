package ru.practicum.shareit.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapperImpl;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.comment.validator.CommentValidator;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Интеграционные тесты для сервиса комментариев {@link CommentServiceImpl}.
 * Тестирует все основные функции сервиса комментариев:
 * - Создание комментария к предмету
 * - Получение комментариев по ID предмета
 * - Валидацию возможности создания комментариев
 *
 * Тесты используют реальную базу данных в памяти (H2) через TestEntityManager
 * и моки внешних сервисов (UserService, ItemService) для изоляции тестируемого функционала.
 *
 * Класс использует аннотацию {@link DirtiesContext} для очистки контекста
 * после каждого теста, обеспечивая независимость тестов друг от друга.
 *
 * Для тестирования валидации используется {@link SpyBean} для CommentValidator,
 * что позволяет частично заменять поведение валидатора в отдельных тестах.
 */
@DataJpaTest
@Import({CommentServiceImpl.class, CommentMapperImpl.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentServiceImplIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    @MockBean
    private ItemService itemService;

    @MockBean
    private UserService userService;

    @SpyBean
    private CommentValidator commentValidator;

    private User owner;
    private User author;
    private Item item;
    private Comment comment;

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Создает тестовые сущности в базе данных:
     * - Пользователя-владельца предмета
     * - Пользователя-автора комментария
     * - Предмет для комментирования
     * - Тестовый комментарий
     * - Настраивает моки внешних сервисов
     *
     * Моки настроены для возврата созданных сущностей при запросе по ID
     * и выбрасывания исключений при запросе несуществующих сущностей.
     */
    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        owner = entityManager.persistAndFlush(owner);

        author = new User();
        author.setName("Author");
        author.setEmail("author@test.com");
        author = entityManager.persistAndFlush(author);

        item = new Item();
        item.setName("Дрель");
        item.setDescription("Электрическая дрель");
        item.setAvailable(true);
        item.setOwner(owner);
        item = entityManager.persistAndFlush(item);

        when(itemService.getItemById(item.getId())).thenReturn(item);
        when(itemService.getItemById(999L)).thenThrow(new NotFoundException("Item not found"));

        when(userService.getUserById(author.getId())).thenReturn(author);
        when(userService.getUserById(999L)).thenThrow(new NotFoundException("User not found"));

        comment = new Comment();
        comment.setText("Отличная дрель!");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        comment = entityManager.persistAndFlush(comment);
    }

    /**
     * Тест создания нового комментария.
     * Проверяет, что комментарий успешно создается в базе данных
     * с правильными данными и текущей датой создания.
     *
     * Для этого теста отключается валидация возможности создания комментария,
     * чтобы сосредоточиться на проверке самого процесса создания.
     */
    @Test
    void createComment_shouldCreateAndReturnComment() {
        doNothing().when(commentValidator).validateCommentCreation(any(), any());

        Comment newComment = new Comment();
        newComment.setText("Хороший инструмент");

        Comment createdComment = commentService.createComment(item.getId(), newComment, author.getId());

        assertNotNull(createdComment.getId());
        assertEquals("Хороший инструмент", createdComment.getText());
        assertEquals(item.getId(), createdComment.getItem().getId());
        assertEquals(author.getId(), createdComment.getAuthor().getId());
        assertNotNull(createdComment.getCreated());
    }

    /**
     * Тест получения комментариев по ID предмета с пагинацией.
     * Проверяет, что список комментариев успешно возвращается в виде страницы
     * и содержит созданный комментарий с правильными данными.
     */
    @Test
    void getCommentsByItemId_shouldReturnCommentsPage() {
        Pageable pageable = PageRequest.of(0, 10);

        var commentsPage = commentService.getCommentsByItemId(item.getId(), pageable);

        assertNotNull(commentsPage);
        assertEquals(1, commentsPage.getTotalElements());
        CommentDto commentDto = commentsPage.getContent().get(0);
        assertEquals(comment.getId(), commentDto.getId());
        assertEquals(comment.getText(), commentDto.getText());
        assertEquals(author.getName(), commentDto.getAuthorName());
    }

    /**
     * Тест получения комментариев по ID предмета без пагинации.
     * Проверяет, что список всех комментариев предмета успешно возвращается
     * и содержит созданный комментарий с правильными данными.
     */
    @Test
    void getCommentsByItemIdWithAuthor_shouldReturnCommentsList() {
        List<CommentDto> comments = commentService.getCommentsByItemIdWithAuthor(item.getId());

        assertNotNull(comments);
        assertEquals(1, comments.size());
        CommentDto commentDto = comments.get(0);
        assertEquals(comment.getId(), commentDto.getId());
        assertEquals(comment.getText(), commentDto.getText());
        assertEquals(author.getName(), commentDto.getAuthorName());
        assertNotNull(commentDto.getCreated());
    }

    /**
     * Тест создания комментария для несуществующего предмета.
     * Проверяет, что при попытке создать комментарий для несуществующего предмета
     * выбрасывается исключение {@link NotFoundException}.
     */
    @Test
    void createComment_shouldThrowExceptionWhenItemNotFound() {
        Comment newComment = new Comment();
        newComment.setText("Текст комментария");

        assertThrows(NotFoundException.class, () ->
                commentService.createComment(999L, newComment, author.getId()));
    }

    /**
     * Тест создания комментария от несуществующего пользователя.
     * Проверяет, что при попытке создать комментарий от имени несуществующего пользователя
     * выбрасывается исключение {@link NotFoundException}.
     */
    @Test
    void createComment_shouldThrowExceptionWhenUserNotFound() {
        Comment newComment = new Comment();
        newComment.setText("Текст комментария");

        assertThrows(NotFoundException.class, () ->
                commentService.createComment(item.getId(), newComment, 999L));
    }

    /**
     * Тест создания комментария пользователем, который не арендовал предмет.
     * Проверяет, что при попытке создать комментарий пользователем, который
     * не имеет подтвержденного бронирования предмета, выбрасывается
     * исключение {@link ValidationException}.
     */
    @Test
    void createComment_shouldThrowValidationExceptionWhenUserDidNotRentItem() {
        Comment newComment = new Comment();
        newComment.setText("Текст комментария");

        assertThrows(ValidationException.class, () ->
                commentService.createComment(item.getId(), newComment, author.getId()));
    }
}


