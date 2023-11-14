package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentOutDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * processing HTTP-requests to "/items" end-point to add, update or get items' data and comments to items.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ITEMS_PATH)
@Slf4j
public class ItemController {
    private final ItemService itemService;

    /**
     * processing POST-request to add item's data (save and assign identity)
     *
     * @param userId  owner's id
     * @param itemDto item to save and register
     * @return registered item with assigned id
     */
    @PostMapping
    public ItemDto create(@RequestHeader(HEADER_USER_ID) Long userId,
                          @RequestBody ItemDto itemDto) {
        log.info("POST-request: создание вещи c id: {}, {}", userId, itemDto);
        return itemService.create(userId, itemDto);
    }

    /**
     * processing GET-request to get item by id
     *
     * @param userId user's id
     * @param itemId item's id
     * @return item
     */
    @GetMapping(ITEM_ID_PATH_VARIABLE)
    public ItemOutDto getById(@RequestHeader(HEADER_USER_ID) Long userId,
                              @PathVariable Long itemId) {
        log.info("GET-request: получение информации о вещи по id: {}, запрашивает пользователь: {}", itemId, userId);
        return itemService.getById(userId, itemId);
    }

    /**
     * processing PATCH-request to update item's properties
     *
     * @param userId  owner's id
     * @param itemId  item's id
     * @param itemDto object with properties to update
     * @return updated item
     */
    @PatchMapping(ITEM_ID_PATH_VARIABLE)
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody ItemDto itemDto,
                          @PathVariable Long itemId) {
        log.info("PATCH-request: обновление данных о вещи: {}, от пользователя: {}, данные для обновления: {}",
                itemId, userId, itemDto);
        return itemService.update(userId, itemDto, itemId);
    }

    /**
     * processing DELETE-request to delete item
     *
     * @param itemId item's id
     */
    @DeleteMapping(ITEM_ID_PATH_VARIABLE)
    public void delete(@PathVariable Long itemId) {
        log.info("DELETE-request: удаление информации о вещи с id: {}", itemId);
        itemService.deleteById(itemId);
    }

    /**
     * processing GET-request to get all items of a specific user
     *
     * @param userId user's id
     * @return list of items
     */
    @GetMapping()
    public List<ItemOutDto> getListByUser(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("GET-request: получение списка вещей пользователя с id: {}", userId);
        return itemService.getListByUser(userId);
    }

    /**
     * processing GET-request to search all available items, contained substring in name or description
     *
     * @param substring substring for search
     * @return list of ItemDto objects
     */
    @GetMapping(SEARCH_PATH)
    public List<ItemOutDto> searchItemsBySubstring(@RequestParam(TEXT_PARAMETER_NAME) String substring) {
        log.info("GET-request: получение списка доступных к бронированию вещей,"
                + " содержащих в описании или названии подстроку: {}", substring);
        return itemService.searchItemsBySubstring(substring);
    }

    /**
     * processing POST-request to add comment to a specific item
     *
     * @param commentDto comment
     * @param userId     author's id
     * @param itemId     item's id
     * @return registered comment with assigned id
     */
    @PostMapping(COMMENT_PATH_VARIABLE)
    public CommentOutDto addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @RequestBody CommentDto commentDto,
                                    @PathVariable long itemId) {
        log.info("POST-request добавление нового отзыва от пользователя {} вещи {}, шаблон отзыва: {}",
                userId, itemId, commentDto);
        return itemService.addComment(commentDto, userId, itemId);
    }
}