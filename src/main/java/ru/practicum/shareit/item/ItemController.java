package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.groups.Create;

import javax.validation.Valid;
import java.util.List;

/**
 * Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;

    /**
     * create ItemDto
     *
     * @param userId  owner's id
     * @param itemDto ItemDto object to register
     * @return registered ItemDto object
     */
    @PostMapping()
    @Validated({Create.class})
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("POST-request: создание вещи c id: {}, {}", userId, itemDto);
        return itemService.create(userId, itemDto);
    }

    /**
     * get ItemDto object
     *
     * @param userId owner's id
     * @param itemId item's id
     * @return ItemDto object
     */
    @GetMapping("/{itemId}")
    public ItemResponseDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @PathVariable Long itemId) {
        log.info("GET-request: получение вещи по id вещи: {}, владелец вещи: {}", itemId, userId);
        return itemService.getById(userId, itemId);
    }

    /**
     * update ItemDto object
     *
     * @param userId  owner's id
     * @param itemId  item's id
     * @param itemDto ItemDto object with properties to update
     * @return updated ItemDto object
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
     * delete ItemDto object
     *
     * @param itemId item's id
     */
    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long itemId) {
        log.info("DELETE-request: удаление информации о вещи с id: {}", itemId);
        itemService.deleteById(itemId);
    }

    /**
     * get all items of a specific user
     *
     * @param userId user's id
     * @return list of ItemDto objects
     */
    @GetMapping()
    public List<ItemResponseDto> getListByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET-request: получение списка вещей пользователя с id: {}", userId);
        return itemService.getListByUser(userId);
    }

    /**
     * search all available items, contained substring in name or description
     *
     * @param substring substring for search
     * @return list of ItemDto objects
     */
    @GetMapping("/search")
    public List<ItemResponseDto> searchItemsBySubstring(@RequestParam("text") String substring) {
        log.info("GET-request: получение списка доступных к бронированию вещей,"
                + " содержащих в описании или названии подстроку: {}", substring);
        return itemService.searchItemsBySubstring(substring);
    }

    /**
     * add comment to a specific item
     *
     * @param commentRequestDto CommentRequestDto object
     * @param userId            author's id
     * @param itemId            item's id
     * @return CommentResponseDto object
     */
    @PostMapping("{itemId}/comment")
    @Validated({Create.class})
    public CommentResponseDto addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody CommentRequestDto commentRequestDto,
                                         @PathVariable long itemId) {
        log.info("POST-request добавление нового отзыва от пользователя {} вещи {}, шаблон отзыва: {}",
                userId, itemId, commentRequestDto);
        return itemService.addComment(commentRequestDto, userId, itemId);
    }
}