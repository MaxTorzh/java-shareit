package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemMapperImplTest {

    @Autowired
    private ItemMapper itemMapper;

    private Item item;
    private ItemRequestDto itemRequestDto;
    private User owner;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Test Owner");
        owner.setEmail("owner@test.com");

        request = new ItemRequest();
        request.setId(1L);

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("Test Item");
        itemRequestDto.setDescription("Test Description");
        itemRequestDto.setAvailable(true);
    }

    /**
     * Тест преобразования Item в ItemDto.
     * Проверяет успешное преобразование всех полей.
     */
    @Test
    void toDto_shouldConvertItemToItemDto() {
        ItemDto result = itemMapper.toDto(item);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(owner.getId(), result.getOwnerId());
        assertEquals(request.getId(), result.getRequestId());
    }

    /**
     * Тест преобразования Item в ItemDto с null Item.
     * Проверяет обработку null значения.
     */
    @Test
    void toDto_shouldReturnNullWhenItemIsNull() {
        ItemDto result = itemMapper.toDto(null);

        assertNull(result);
    }

    /**
     * Тест преобразования Item в ItemDto с null Owner.
     * Проверяет обработку null значения в поле owner.
     */
    @Test
    void toDto_shouldHandleNullOwner() {
        item.setOwner(null);

        ItemDto result = itemMapper.toDto(item);

        assertNotNull(result);
        assertNull(result.getOwnerId());
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(request.getId(), result.getRequestId());
    }

    /**
     * Тест преобразования Item в ItemDto с null Request.
     * Проверяет обработку null значения в поле request.
     */
    @Test
    void toDto_shouldHandleNullRequest() {
        item.setRequest(null);

        ItemDto result = itemMapper.toDto(item);

        assertNotNull(result);
        assertNull(result.getRequestId());
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(owner.getId(), result.getOwnerId());
    }

    /**
     * Тест преобразования Item в ItemDto с null Owner ID.
     * Проверяет обработку null значения в поле owner.id.
     */
    @Test
    void toDto_shouldHandleNullOwnerId() {
        owner.setId(null);
        item.setOwner(owner);

        ItemDto result = itemMapper.toDto(item);

        assertNotNull(result);
        assertNull(result.getOwnerId());
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(request.getId(), result.getRequestId());
    }

    /**
     * Тест преобразования Item в ItemDto с null Request ID.
     * Проверяет обработку null значения в поле request.id.
     */
    @Test
    void toDto_shouldHandleNullRequestId() {
        request.setId(null);
        item.setRequest(request);

        ItemDto result = itemMapper.toDto(item);

        assertNotNull(result);
        assertNull(result.getRequestId());
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(owner.getId(), result.getOwnerId());
    }

    /**
     * Тест преобразования Item в ItemWithBookingsDto.
     * Проверяет успешное преобразование всех полей.
     */
    @Test
    void toWithBookingsDto_shouldConvertItemToItemWithBookingsDto() {
        ItemWithBookingsDto result = itemMapper.toWithBookingsDto(item);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(owner.getId(), result.getOwnerId());
        assertEquals(request.getId(), result.getRequestId());
    }

    /**
     * Тест преобразования Item в ItemWithBookingsDto с null Item.
     * Проверяет обработку null значения.
     */
    @Test
    void toWithBookingsDto_shouldReturnNullWhenItemIsNull() {
        ItemWithBookingsDto result = itemMapper.toWithBookingsDto(null);

        assertNull(result);
    }

    /**
     * Тест преобразования Item в ItemWithBookingsDto с null Owner.
     * Проверяет обработку null значения в поле owner.
     */
    @Test
    void toWithBookingsDto_shouldHandleNullOwner() {
        item.setOwner(null);

        ItemWithBookingsDto result = itemMapper.toWithBookingsDto(item);

        assertNotNull(result);
        assertNull(result.getOwnerId());
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(request.getId(), result.getRequestId());
    }

    /**
     * Тест преобразования Item в ItemWithBookingsDto с null Request.
     * Проверяет обработку null значения в поле request.
     */
    @Test
    void toWithBookingsDto_shouldHandleNullRequest() {
        item.setRequest(null);

        ItemWithBookingsDto result = itemMapper.toWithBookingsDto(item);

        assertNotNull(result);
        assertNull(result.getRequestId());
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(owner.getId(), result.getOwnerId());
    }

    /**
     * Тест преобразования ItemRequestDto в Item.
     * Проверяет успешное преобразование всех полей.
     */
    @Test
    void toItem_shouldConvertItemRequestDtoToItem() {
        Item result = itemMapper.toItem(itemRequestDto, owner, request);

        assertNotNull(result);
        assertEquals(itemRequestDto.getName(), result.getName());
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        assertEquals(itemRequestDto.getAvailable(), result.getAvailable());
        assertEquals(owner, result.getOwner());
        assertEquals(request, result.getRequest());
        assertNull(result.getId());
    }

    /**
     * Тест преобразования ItemRequestDto в Item с null параметрами.
     * Проверяет обработку null значений.
     */
    @Test
    void toItem_shouldReturnNullWhenAllParametersAreNull() {
        Item result = itemMapper.toItem(null, null, null);

        assertNull(result);
    }

    /**
     * Тест преобразования ItemRequestDto в Item с null DTO, но с валидными owner и request.
     * Проверяет обработку частично null значений.
     */
    @Test
    void toItem_shouldCreateItemWithNullDtoButValidOwnerAndRequest() {
        Item result = itemMapper.toItem(null, owner, request);

        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getDescription());
        assertNull(result.getAvailable());
        assertEquals(owner, result.getOwner());
        assertEquals(request, result.getRequest());
    }

    /**
     * Тест преобразования ItemRequestDto в Item с валидным DTO, но null owner и request.
     * Проверяет обработку частично null значений.
     */
    @Test
    void toItem_shouldCreateItemWithValidDtoButNullOwnerAndRequest() {
        Item result = itemMapper.toItem(itemRequestDto, null, null);

        assertNotNull(result);
        assertEquals(itemRequestDto.getName(), result.getName());
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        assertEquals(itemRequestDto.getAvailable(), result.getAvailable());
        assertNull(result.getOwner());
        assertNull(result.getRequest());
    }

    /**
     * Тест преобразования ItemRequestDto в Item с пустыми значениями.
     * Проверяет обработку пустых значений.
     */
    @Test
    void toItem_shouldHandleEmptyValues() {
        itemRequestDto.setName("");
        itemRequestDto.setDescription("");
        itemRequestDto.setAvailable(null);

        Item result = itemMapper.toItem(itemRequestDto, owner, request);

        assertNotNull(result);
        assertEquals("", result.getName());
        assertEquals("", result.getDescription());
        assertNull(result.getAvailable());
        assertEquals(owner, result.getOwner());
        assertEquals(request, result.getRequest());
    }

    /**
     * Тест преобразования Item с пустыми значениями.
     * Проверяет обработку граничных условий.
     */
    @Test
    void toDto_shouldHandleItemWithEmptyValues() {
        Item emptyItem = new Item();
        emptyItem.setId(null);
        emptyItem.setName("");
        emptyItem.setDescription("");
        emptyItem.setAvailable(null);
        emptyItem.setOwner(null);
        emptyItem.setRequest(null);

        ItemDto result = itemMapper.toDto(emptyItem);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("", result.getName());
        assertEquals("", result.getDescription());
        assertNull(result.getAvailable());
        assertNull(result.getOwnerId());
        assertNull(result.getRequestId());
    }

    /**
     * Тест преобразования ItemWithBookingsDto с пустыми значениями.
     * Проверяет обработку граничных условий.
     */
    @Test
    void toWithBookingsDto_shouldHandleItemWithEmptyValues() {
        Item emptyItem = new Item();
        emptyItem.setId(null);
        emptyItem.setName("");
        emptyItem.setDescription("");
        emptyItem.setAvailable(null);
        emptyItem.setOwner(null);
        emptyItem.setRequest(null);

        ItemWithBookingsDto result = itemMapper.toWithBookingsDto(emptyItem);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("", result.getName());
        assertEquals("", result.getDescription());
        assertNull(result.getAvailable());
        assertNull(result.getOwnerId());
        assertNull(result.getRequestId());
    }

    /**
     * Тест преобразования ItemRequestDto с пустыми значениями.
     * Проверяет обработку граничных условий.
     */
    @Test
    void toItem_shouldHandleItemRequestDtoWithEmptyValues() {
        ItemRequestDto emptyDto = new ItemRequestDto();
        emptyDto.setName("");
        emptyDto.setDescription("");
        emptyDto.setAvailable(null);

        Item result = itemMapper.toItem(emptyDto, owner, request);

        assertNotNull(result);
        assertEquals("", result.getName());
        assertEquals("", result.getDescription());
        assertNull(result.getAvailable());
        assertEquals(owner, result.getOwner());
        assertEquals(request, result.getRequest());
    }

    /**
     * Тест преобразования ItemRequestDto с null значениями.
     * Проверяет обработку null значений.
     */
    @Test
    void toItem_shouldHandleItemRequestDtoWithNullValues() {
        ItemRequestDto nullDto = new ItemRequestDto();
        nullDto.setName(null);
        nullDto.setDescription(null);
        nullDto.setAvailable(null);

        Item result = itemMapper.toItem(nullDto, owner, request);

        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getDescription());
        assertNull(result.getAvailable());
        assertEquals(owner, result.getOwner());
        assertEquals(request, result.getRequest());
    }

    /**
     * Тест производительности преобразования.
     * Проверяет обработку большого количества преобразований.
     */
    @Test
    void toDto_shouldHandleMultipleConversions() {
        for (int i = 0; i < 100; i++) {
            Item testItem = new Item();
            testItem.setId((long) i);
            testItem.setName("Item " + i);
            testItem.setDescription("Description " + i);
            testItem.setAvailable(i % 2 == 0);
            testItem.setOwner(owner);
            testItem.setRequest(request);

            ItemDto result = itemMapper.toDto(testItem);

            assertNotNull(result);
            assertEquals((long) i, result.getId());
            assertEquals("Item " + i, result.getName());
            assertEquals("Description " + i, result.getDescription());
            assertEquals(i % 2 == 0, result.getAvailable());
        }
    }
}

