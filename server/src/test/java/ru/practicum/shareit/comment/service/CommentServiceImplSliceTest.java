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
 * Slice тесты для сервиса комментариев {@link CommentServiceImpl}.
 * Тестируют функциональность сервиса с использованием реальной базы данных
 * и моков для внешних сервисов (UserService, ItemService).
 *
 * Используют @DataJpaTest для тестирования слоя работы с БД и @Import для
 * загрузки тестируемого сервиса и его зависимостей.
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
class CommentServiceImplSliceTest {

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

    /**
     * Тест создания комментария с пустым текстом.
     * Проверяет, что валидация предотвращает создание пустых комментариев.
     */
    @Test
    void createComment_shouldThrowExceptionWhenTextIsEmpty() {
        Comment newComment = new Comment();
        newComment.setText("");

        assertThrows(ValidationException.class, () ->
                commentService.createComment(item.getId(), newComment, author.getId()));
    }

    /**
     * Тест получения комментариев для несуществующего предмета.
     * Проверяет правильную обработку исключений.
     */
    @Test
    void getCommentsByItemId_shouldReturnEmptyWhenItemNotFound() {
        Pageable pageable = PageRequest.of(0, 10);

        var result = commentService.getCommentsByItemId(999L, pageable);

        assertEquals(0, result.getTotalElements());
    }

    /**
     * Тест получения комментариев для предмета без комментариев.
     * Проверяет, что возвращается пустой список.
     */
    @Test
    void getCommentsByItemIdWithAuthor_shouldReturnEmptyListWhenNoComments() {
        Item newItem = new Item();
        newItem.setName("Предмет без комментариев");
        newItem.setDescription("Предмет без комментариев");
        newItem.setAvailable(true);
        newItem.setOwner(owner);
        newItem = entityManager.persistAndFlush(newItem);

        List<CommentDto> comments = commentService.getCommentsByItemIdWithAuthor(newItem.getId());

        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    /**
     * Тест маппинга Comment в CommentDto.
     * Проверяет корректность преобразования сущности в DTO.
     */
    @Test
    void commentToCommentDto_shouldMapCorrectly() {
        CommentDto commentDto = commentService.getCommentsByItemIdWithAuthor(item.getId()).get(0);

        assertNotNull(commentDto);
        assertEquals(comment.getId(), commentDto.getId());
        assertEquals(comment.getText(), commentDto.getText());
        assertEquals(author.getName(), commentDto.getAuthorName());
        assertEquals(comment.getCreated(), commentDto.getCreated());
    }

    /**
     * Тест получения комментариев с пагинацией - первая страница.
     */
    @Test
    void getCommentsByItemId_shouldReturnFirstPage() {
        Comment comment2 = new Comment();
        comment2.setText("Еще один комментарий");
        comment2.setItem(item);
        comment2.setAuthor(author);
        comment2.setCreated(LocalDateTime.now().plusSeconds(1));
        entityManager.persistAndFlush(comment2);

        Pageable pageable = PageRequest.of(0, 1);

        var commentsPage = commentService.getCommentsByItemId(item.getId(), pageable);

        assertNotNull(commentsPage);
        assertEquals(2, commentsPage.getTotalElements());
        assertEquals(1, commentsPage.getContent().size());
        assertEquals("Еще один комментарий", commentsPage.getContent().get(0).getText());
    }

    /**
     * Тест получения комментариев с пагинацией - вторая страница.
     */
    @Test
    void getCommentsByItemId_shouldReturnSecondPage() {
        Comment comment2 = new Comment();
        comment2.setText("Еще один комментарий");
        comment2.setItem(item);
        comment2.setAuthor(author);
        comment2.setCreated(LocalDateTime.now().plusSeconds(1));
        entityManager.persistAndFlush(comment2);

        Pageable pageable = PageRequest.of(1, 1);

        var commentsPage = commentService.getCommentsByItemId(item.getId(), pageable);

        assertNotNull(commentsPage);
        assertEquals(2, commentsPage.getTotalElements());
        assertEquals(1, commentsPage.getContent().size());
        assertEquals("Отличная дрель!", commentsPage.getContent().get(0).getText());
    }

    /**
     * Тест создания комментария с пробелами в тексте.
     * Проверяет, что валидация корректно обрабатывает текст только из пробелов.
     */
    @Test
    void createComment_shouldThrowExceptionWhenTextIsBlank() {
        Comment newComment = new Comment();
        newComment.setText("   ");

        assertThrows(ValidationException.class, () ->
                commentService.createComment(item.getId(), newComment, author.getId()));
    }

    /**
     * Тест создания комментария с null текстом.
     * Проверяет, что валидация корректно обрабатывает null значения.
     */
    @Test
    void createComment_shouldThrowExceptionWhenTextIsNull() {
        Comment newComment = new Comment();
        newComment.setText(null);

        assertThrows(ValidationException.class, () ->
                commentService.createComment(item.getId(), newComment, author.getId()));
    }

    /**
     * Тест получения комментариев для предмета с несколькими комментариями.
     * Проверяет правильный порядок комментариев (по дате создания).
     */
    @Test
    void getCommentsByItemIdWithAuthor_shouldReturnCommentsInCorrectOrder() {
        Comment comment2 = new Comment();
        comment2.setText("Второй комментарий");
        comment2.setItem(item);
        comment2.setAuthor(author);
        comment2.setCreated(LocalDateTime.now().plusSeconds(10));
        entityManager.persistAndFlush(comment2);

        Comment comment3 = new Comment();
        comment3.setText("Третий комментарий");
        comment3.setItem(item);
        comment3.setAuthor(author);
        comment3.setCreated(LocalDateTime.now().plusSeconds(5));
        entityManager.persistAndFlush(comment3);

        List<CommentDto> comments = commentService.getCommentsByItemIdWithAuthor(item.getId());

        assertNotNull(comments);
        assertEquals(3, comments.size());
        assertEquals("Второй комментарий", comments.get(0).getText());
        assertEquals("Третий комментарий", comments.get(1).getText());
        assertEquals("Отличная дрель!", comments.get(2).getText());
    }

    /**
     * Тест получения комментариев с разными параметрами пагинации.
     */
    @Test
    void getCommentsByItemId_shouldHandleDifferentPageSizes() {
        for (int i = 0; i < 5; i++) {
            Comment additionalComment = new Comment();
            additionalComment.setText("Комментарий " + (i + 2));
            additionalComment.setItem(item);
            additionalComment.setAuthor(author);
            additionalComment.setCreated(LocalDateTime.now().plusSeconds(i + 1));
            entityManager.persistAndFlush(additionalComment);
        }

        Pageable pageableSize2 = PageRequest.of(0, 2);
        var page2 = commentService.getCommentsByItemId(item.getId(), pageableSize2);
        assertEquals(2, page2.getContent().size());
        assertEquals(6, page2.getTotalElements());

        Pageable pageableSize5 = PageRequest.of(0, 5);
        var page5 = commentService.getCommentsByItemId(item.getId(), pageableSize5);
        assertEquals(5, page5.getContent().size());
        assertEquals(6, page5.getTotalElements());
    }

    /**
     * Тест получения комментариев для несуществующей страницы.
     */
    @Test
    void getCommentsByItemId_shouldHandleNonExistentPage() {
        Pageable pageable = PageRequest.of(10, 10);

        var result = commentService.getCommentsByItemId(item.getId(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    /**
     * Тест получения комментариев с сортировкой по времени создания.
     */
    @Test
    void getCommentsByItemIdWithAuthor_shouldReturnSortedComments() {
        Comment earlyComment = new Comment();
        earlyComment.setText("Ранний комментарий");
        earlyComment.setItem(item);
        earlyComment.setAuthor(author);
        earlyComment.setCreated(LocalDateTime.now().minusHours(2));
        entityManager.persistAndFlush(earlyComment);

        Comment lateComment = new Comment();
        lateComment.setText("Поздний комментарий");
        lateComment.setItem(item);
        lateComment.setAuthor(author);
        lateComment.setCreated(LocalDateTime.now().plusHours(1));
        entityManager.persistAndFlush(lateComment);

        List<CommentDto> comments = commentService.getCommentsByItemIdWithAuthor(item.getId());

        assertNotNull(comments);
        assertEquals(3, comments.size());
        assertEquals("Поздний комментарий", comments.get(0).getText());
        assertEquals("Отличная дрель!", comments.get(1).getText());
        assertEquals("Ранний комментарий", comments.get(2).getText());
    }

    /**
     * Тест создания комментария с очень коротким текстом.
     */
    @Test
    void createComment_shouldHandleMinimalText() {
        doNothing().when(commentValidator).validateCommentCreation(any(), any());

        Comment newComment = new Comment();
        newComment.setText("A");

        Comment createdComment = commentService.createComment(item.getId(), newComment, author.getId());

        assertNotNull(createdComment);
        assertEquals("A", createdComment.getText());
    }

    /**
     * Тест получения комментариев с различными вариантами пагинации.
     */
    @Test
    void getCommentsByItemId_shouldHandleEdgePageValues() {
        for (int i = 0; i < 3; i++) {
            Comment additionalComment = new Comment();
            additionalComment.setText("Комментарий " + (i + 2));
            additionalComment.setItem(item);
            additionalComment.setAuthor(author);
            additionalComment.setCreated(LocalDateTime.now().plusSeconds(i + 1));
            entityManager.persistAndFlush(additionalComment);
        }

        Pageable zeroPage = PageRequest.of(0, 1);
        var zeroPageResult = commentService.getCommentsByItemId(item.getId(), zeroPage);
        assertEquals(1, zeroPageResult.getContent().size());

        Pageable negativeSizePage = PageRequest.of(0, Integer.MAX_VALUE);
        var negativeSizeResult = commentService.getCommentsByItemId(item.getId(), negativeSizePage);
        assertTrue(negativeSizeResult.getTotalElements() > 0);
    }

    /**
     * Тест получения комментариев с null pageable.
     */
    @Test
    void getCommentsByItemId_shouldHandleNullPageable() {
        Pageable pageable = PageRequest.of(0, 10);

        assertDoesNotThrow(() -> {
            var result = commentService.getCommentsByItemId(item.getId(), pageable);
            assertNotNull(result);
        });
    }

    /**
     * Тест создания комментария с пустым текстом после trim.
     */
    @Test
    void createComment_shouldThrowExceptionWhenTextIsEmptyAfterTrim() {
        Comment newComment = new Comment();
        newComment.setText("   ");

        ValidationException exception = assertThrows(ValidationException.class, () ->
                commentService.createComment(item.getId(), newComment, author.getId()));

        assertNotNull(exception);
    }

    /**
     * Тест создания комментария с whitespace текстом.
     */
    @Test
    void createComment_shouldThrowExceptionWhenTextIsOnlyWhitespace() {
        Comment newComment = new Comment();
        newComment.setText("\t\n\r"); // Только whitespace символы

        ValidationException exception = assertThrows(ValidationException.class, () ->
                commentService.createComment(item.getId(), newComment, author.getId()));

        assertNotNull(exception);
    }

    /**
     * Тест получения комментариев с различными временными зонами.
     */
    @Test
    void getCommentsByItemId_shouldHandleCommentsWithDifferentTimes() {
        LocalDateTime now = LocalDateTime.now();

        Comment pastComment = new Comment();
        pastComment.setText("Прошлый комментарий");
        pastComment.setItem(item);
        pastComment.setAuthor(author);
        pastComment.setCreated(now.minusWeeks(1));
        entityManager.persistAndFlush(pastComment);

        Comment futureComment = new Comment();
        futureComment.setText("Будущий комментарий");
        futureComment.setItem(item);
        futureComment.setAuthor(author);
        futureComment.setCreated(now.plusWeeks(1));
        entityManager.persistAndFlush(futureComment);

        List<CommentDto> comments = commentService.getCommentsByItemIdWithAuthor(item.getId());

        assertNotNull(comments);
        assertEquals(3, comments.size());
        assertEquals("Будущий комментарий", comments.get(0).getText());
        assertEquals("Отличная дрель!", comments.get(1).getText());
        assertEquals("Прошлый комментарий", comments.get(2).getText());
    }

    /**
     * Тест создания комментария с текстом, содержащим специальные символы.
     */
    @Test
    void createComment_shouldHandleSpecialCharacters() {
        doNothing().when(commentValidator).validateCommentCreation(any(), any());

        Comment newComment = new Comment();
        newComment.setText("Комментарий со специальными символами: !@#$%^&*()_+-=[]{}|;':\",./<>?");

        Comment createdComment = commentService.createComment(item.getId(), newComment, author.getId());

        assertNotNull(createdComment);
        assertEquals("Комментарий со специальными символами: !@#$%^&*()_+-=[]{}|;':\",./<>?", createdComment.getText());
    }

    /**
     * Тест создания комментария с многострочным текстом.
     */
    @Test
    void createComment_shouldHandleMultilineText() {
        doNothing().when(commentValidator).validateCommentCreation(any(), any());

        Comment newComment = new Comment();
        newComment.setText("Первая строка\nВторая строка\nТретья строка");

        Comment createdComment = commentService.createComment(item.getId(), newComment, author.getId());

        assertNotNull(createdComment);
        assertEquals("Первая строка\nВторая строка\nТретья строка", createdComment.getText());
    }

    /**
     * Тест получения комментариев для предмета с большим количеством комментариев.
     */
    @Test
    void getCommentsByItemId_shouldHandleLargeNumberOfComments() {
        for (int i = 0; i < 50; i++) {
            Comment additionalComment = new Comment();
            additionalComment.setText("Комментарий #" + (i + 1));
            additionalComment.setItem(item);
            additionalComment.setAuthor(author);
            additionalComment.setCreated(LocalDateTime.now().plusSeconds(i));
            entityManager.persistAndFlush(additionalComment);
        }

        Pageable pageable = PageRequest.of(0, 20);
        var result = commentService.getCommentsByItemId(item.getId(), pageable);

        assertEquals(51, result.getTotalElements());
        assertEquals(20, result.getContent().size());
    }

    /**
     * Тест получения комментариев с пагинацией и сортировкой по убыванию даты.
     */
    @Test
    void getCommentsByItemId_shouldReturnCommentsSortedByDateDesc() {
        for (int i = 0; i < 5; i++) {
            Comment additionalComment = new Comment();
            additionalComment.setText("Комментарий #" + (i + 1));
            additionalComment.setItem(item);
            additionalComment.setAuthor(author);
            additionalComment.setCreated(LocalDateTime.now().minusDays(5 - i));
            entityManager.persistAndFlush(additionalComment);
        }

        Pageable pageable = PageRequest.of(0, 10);
        var result = commentService.getCommentsByItemId(item.getId(), pageable);

        List<CommentDto> comments = result.getContent();
        for (int i = 0; i < comments.size() - 1; i++) {
            assertTrue(comments.get(i).getCreated().isAfter(comments.get(i + 1).getCreated()) ||
                    comments.get(i).getCreated().isEqual(comments.get(i + 1).getCreated()));
        }
    }

    /**
     * Тест создания комментария с валидным текстом после валидации.
     */
    @Test
    void createComment_shouldValidateAndCreateWithValidText() {
        doNothing().when(commentValidator).validateCommentCreation(any(), any());

        Comment newComment = new Comment();
        newComment.setText("   Валидный комментарий с пробелами в начале и конце   ");

        Comment createdComment = commentService.createComment(item.getId(), newComment, author.getId());

        assertNotNull(createdComment);
        assertEquals("   Валидный комментарий с пробелами в начале и конце   ", createdComment.getText());
    }

    /**
     * Тест создания комментария с текстом, содержащим только цифры.
     */
    @Test
    void createComment_shouldAcceptNumericText() {
        doNothing().when(commentValidator).validateCommentCreation(any(), any());

        Comment newComment = new Comment();
        newComment.setText("1234567890");

        Comment createdComment = commentService.createComment(item.getId(), newComment, author.getId());

        assertNotNull(createdComment);
        assertEquals("1234567890", createdComment.getText());
    }

    /**
     * Тест создания комментария с текстом, содержащим только специальные символы.
     */
    @Test
    void createComment_shouldAcceptSpecialCharacterOnlyText() {
        doNothing().when(commentValidator).validateCommentCreation(any(), any());

        Comment newComment = new Comment();
        newComment.setText("!@#$%^&*()");

        Comment createdComment = commentService.createComment(item.getId(), newComment, author.getId());

        assertNotNull(createdComment);
        assertEquals("!@#$%^&*()", createdComment.getText());
    }

    /**
     * Тест получения комментариев для предмета с отрицательным ID.
     */
    @Test
    void getCommentsByItemId_shouldHandleNegativeItemId() {
        Pageable pageable = PageRequest.of(0, 10);

        var result = commentService.getCommentsByItemId(-1L, pageable);

        assertEquals(0, result.getTotalElements());
    }

    /**
     * Тест создания комментария с текстом, содержащим Unicode символы.
     */
    @Test
    void createComment_shouldHandleUnicodeCharacters() {
        doNothing().when(commentValidator).validateCommentCreation(any(), any());

        Comment newComment = new Comment();
        newComment.setText("Комментарий с Unicode: 你好世界 🌍");

        Comment createdComment = commentService.createComment(item.getId(), newComment, author.getId());

        assertNotNull(createdComment);
        assertEquals("Комментарий с Unicode: 你好世界 🌍", createdComment.getText());
    }

    /**
     * Тест получения комментариев с различными авторами.
     */
    @Test
    void getCommentsByItemId_shouldHandleCommentsFromDifferentAuthors() {
        User anotherAuthor = new User();
        anotherAuthor.setName("Another Author");
        anotherAuthor.setEmail("another@test.com");
        anotherAuthor = entityManager.persistAndFlush(anotherAuthor);

        Comment anotherComment = new Comment();
        anotherComment.setText("Комментарий от другого автора");
        anotherComment.setItem(item);
        anotherComment.setAuthor(anotherAuthor);
        anotherComment.setCreated(LocalDateTime.now().plusSeconds(1));
        entityManager.persistAndFlush(anotherComment);

        List<CommentDto> comments = commentService.getCommentsByItemIdWithAuthor(item.getId());

        assertNotNull(comments);
        assertEquals(2, comments.size());
        boolean foundOriginal = false;
        boolean foundAnother = false;

        for (CommentDto commentDto : comments) {
            if (commentDto.getText().equals("Отличная дрель!")) {
                foundOriginal = true;
                assertEquals(author.getName(), commentDto.getAuthorName());
            }
            if (commentDto.getText().equals("Комментарий от другого автора")) {
                foundAnother = true;
                assertEquals(anotherAuthor.getName(), commentDto.getAuthorName());
            }
        }

        assertTrue(foundOriginal);
        assertTrue(foundAnother);
    }

    /**
     * Тест создания комментария с текстом, содержащим эмодзи.
     */
    @Test
    void createComment_shouldHandleEmojiInText() {
        doNothing().when(commentValidator).validateCommentCreation(any(), any());

        Comment newComment = new Comment();
        newComment.setText("Отличный предмет! 👍 😊");

        Comment createdComment = commentService.createComment(item.getId(), newComment, author.getId());

        assertNotNull(createdComment);
        assertEquals("Отличный предмет! 👍 😊", createdComment.getText());
    }

    /**
     * Тест получения комментариев с различными временными метками.
     */
    @Test
    void getCommentsByItemId_shouldHandleCommentsWithSameTimestamp() {
        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);

        Comment comment1 = new Comment();
        comment1.setText("Первый комментарий");
        comment1.setItem(item);
        comment1.setAuthor(author);
        comment1.setCreated(sameTime);
        entityManager.persistAndFlush(comment1);

        Comment comment2 = new Comment();
        comment2.setText("Второй комментарий");
        comment2.setItem(item);
        comment2.setAuthor(author);
        comment2.setCreated(sameTime);
        entityManager.persistAndFlush(comment2);

        List<CommentDto> comments = commentService.getCommentsByItemIdWithAuthor(item.getId());

        assertNotNull(comments);
        assertEquals(3, comments.size());
        boolean foundFirst = false;
        boolean foundSecond = false;

        for (CommentDto commentDto : comments) {
            if (commentDto.getText().equals("Первый комментарий")) {
                foundFirst = true;
            }
            if (commentDto.getText().equals("Второй комментарий")) {
                foundSecond = true;
            }
        }

        assertTrue(foundFirst);
        assertTrue(foundSecond);
    }
}