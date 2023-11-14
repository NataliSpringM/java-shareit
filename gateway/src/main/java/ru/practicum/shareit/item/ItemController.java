package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.util.groups.Create;

import javax.validation.Valid;

import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * processing HTTP-requests to "/items" end-point to add, update or get items' data and comments to items.
 */
@Controller
@RequiredArgsConstructor
@Validated
@RequestMapping(ITEMS_PATH)
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    /**
     * processing POST-request to add item's data
     *
     * @param userId  owner's id
     * @param itemDto item to save and register
     * @return registered item with assigned id
     */
    @PostMapping
    @Validated({Create.class})
    public ResponseEntity<Object> addItem(@RequestHeader(HEADER_USER_ID) Long userId,
                                          @Valid @RequestBody ItemDto itemDto) {
        log.info("User {} added new item: {}", userId, itemDto);
        return itemClient.addItem(userId, itemDto);
    }

    /**
     * processing GET-request to get item by id
     *
     * @param userId user's id
     * @param itemId item's id
     * @return item
     */
    @GetMapping(ITEM_ID_PATH_VARIABLE)
    public ResponseEntity<Object> getById(@RequestHeader(HEADER_USER_ID) Long userId,
                                          @PathVariable Long itemId) {
        log.info("User {} requests info about an item by id: {}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
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
    public ResponseEntity<Object> update(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @RequestBody ItemDto itemDto,
                                         @PathVariable Long itemId) {
        log.info("Updating item with id: {}, ownerId: {}, data to update: {}",
                itemId, userId, itemDto);
        return itemClient.updateItem(userId, itemDto, itemId);
    }

    /**
     * processing DELETE-request to delete item
     *
     * @param itemId item's id
     */
    @DeleteMapping(ITEM_ID_PATH_VARIABLE)
    public void deleteItemById(@PathVariable Long itemId) {
        log.info("Deleting item by id {}", itemId);
        itemClient.deleteItemById(itemId);
    }

    /**
     * processing GET-request to get all items of a specific user
     *
     * @param userId user's id
     * @return list of items
     */
    @GetMapping
    public ResponseEntity<Object> getListByUser(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Get list of user's items. User's id: {}", userId);
        return itemClient.getListByUserId(userId);
    }

    /**
     * processing GET-request to search all available items, contained substring in name or description
     *
     * @param substring substring for search
     * @return list of ItemDto objects
     */
    @GetMapping(SEARCH_PATH)
    public ResponseEntity<Object> searchItemsBySubstring(@RequestParam(TEXT_PARAMETER_NAME) String substring) {
        log.info("Get list of the available items, contains in the name or description text: {}", substring);
        return itemClient.searchItemsBySubstring(substring);
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
    @Validated({Create.class})
    public ResponseEntity<Object> addComment(@RequestHeader(HEADER_USER_ID) Long userId,
                                             @Valid @RequestBody CommentDto commentDto,
                                             @PathVariable Long itemId) {
        log.info("User {} added comment: {} about the item with id {}", userId, commentDto, itemId);
        return itemClient.addComment(commentDto, userId, itemId);
    }
}