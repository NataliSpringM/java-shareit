package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentOutDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.groups.Create;

import javax.validation.Valid;
import java.util.List;

/**
 * Sprint add-controllers.
 * processing HTTP-requests to "/items" end-point to add, update or get items' data and comments to items.
 */
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/items")
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
    @PostMapping()
    @Validated({Create.class})
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("POST-request: создание вещи c id: {}, {}", userId, itemDto);
        return itemService.create(userId, itemDto);
    }

    /**
     * processing GET-request to get item by id
     *
     * @param userId owner's id
     * @param itemId item's id
     * @return item
     */
    @GetMapping("/{itemId}")
    public ItemOutDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long itemId) {
        log.info("GET-request: получение вещи по id вещи: {}, владелец вещи: {}", itemId, userId);
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
    @PatchMapping("/{itemId}")
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
    @DeleteMapping("/{itemId}")
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
    public List<ItemOutDto> getListByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET-request: получение списка вещей пользователя с id: {}", userId);
        return itemService.getListByUser(userId);
    }

    /**
     * processing GET-request to search all available items, contained substring in name or description
     *
     * @param substring substring for search
     * @return list of ItemDto objects
     */
    @GetMapping("/search")
    public List<ItemOutDto> searchItemsBySubstring(@RequestParam("text") String substring) {
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
    @PostMapping("{itemId}/comment")
    @Validated({Create.class})
    public CommentOutDto addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @Valid @RequestBody CommentDto commentDto,
                                    @PathVariable long itemId) {
        log.info("POST-request добавление нового отзыва от пользователя {} вещи {}, шаблон отзыва: {}",
                userId, itemId, commentDto);
        return itemService.addComment(commentDto, userId, itemId);
    }
}