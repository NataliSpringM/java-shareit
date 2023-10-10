package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
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
        System.out.println("CONTROLLER CREATE ITEM, userID: " + userId + ", itemDto: " + itemDto);
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
    public ItemDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long itemId) {
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
        return itemService.update(userId, itemDto, itemId);
    }

    /**
     * delete ItemDto object
     *
     * @param itemId item's id
     */
    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long itemId) {
        itemService.delete(itemId);
    }

    /**
     * get all items of a specific user
     *
     * @param userId user's id
     * @return list of ItemDto objects
     */
    @GetMapping()
    public List<ItemDto> getListByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getListByUser(userId);
    }

    /**
     * search all available items, contained substring in name or description
     *
     * @param substring substring for search
     * @return list of ItemDto objects
     */
    @GetMapping("/search")
    public List<ItemDto> searchItemsBySubstring(@RequestParam("text") String substring) {

        return itemService.searchItemsBySubstring(substring);
    }


}