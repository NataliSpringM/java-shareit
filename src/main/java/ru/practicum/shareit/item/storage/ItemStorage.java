package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * ItemStorage interface
 */
@Component
public interface ItemStorage {
    /**
     * create Item
     *
     * @param item Item object to register
     * @return registered Item object
     */
    Item create(Item item);

    /**
     * get Item object
     *
     * @param itemId item's id
     * @return Item object
     */
    Item getById(Long itemId);

    /**
     * update Item object
     *
     * @param item Item object with properties to update
     * @return updated Item object
     */
    Item update(Item item);

    /**
     * delete Item object
     *
     * @param itemId item's id
     * @return if delete operation was executed
     */
    boolean delete(Long itemId);

    /**
     * get all items
     *
     * @return list of Item objects
     */
    List<Item> getList();

    /**
     * search all available items, contained substring in name or description
     *
     * @param substring substring for search
     * @return list of Item objects
     */
    List<ItemDto> searchItemsBySubstring(String substring);

    /**
     * checks if item is registered in Storage
     *
     * @param itemId item's id
     */
    void checkItemIdExists(Long itemId);
}
