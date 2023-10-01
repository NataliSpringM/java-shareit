package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

/**
 * ItemService interface
 */
@Component
public interface ItemService {
    /**
     * create ItemDto
     *
     * @param userId  owner's id
     * @param itemDto ItemDto object to register
     * @return registered ItemDto object
     */
    ItemDto create(Long userId, ItemDto itemDto);

    /**
     * get ItemDto object
     *
     * @param userId owner's id
     * @param itemId item's id
     * @return ItemDto object
     */
    ItemDto getById(Long userId, Long itemId);

    /**
     * update ItemDto object
     *
     * @param userId  owner's id
     * @param itemId  item's id
     * @param itemDto ItemDto object with properties to update
     * @return updated ItemDto object
     */
    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    /**
     * delete ItemDto object
     *
     * @param itemId item's id
     */
    void delete(Long itemId);

    /**
     * get all items of a specific user
     *
     * @param userId user's id
     * @return list of ItemDto objects
     */
    List<ItemDto> getListByUser(Long userId);

    /**
     * search all available items, contained substring in name or description
     *
     * @param substring substring for search
     * @return list of ItemDto objects
     */
    List<ItemDto> searchItemsBySubstring(String substring);
}
