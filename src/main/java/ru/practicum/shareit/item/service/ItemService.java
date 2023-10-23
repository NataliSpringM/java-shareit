package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import javax.validation.Valid;
import java.util.List;

/**
 * ItemService interface
 */
@Component
public interface ItemService {
    /**
     * to add item's data (save and assign identity)
     *
     * @param userId  owner's id
     * @param itemDto item to register
     * @return item with assigned id
     */
    ItemDto create(Long userId, @Valid ItemDto itemDto);

    /**
     * get item by id
     *
     * @param userId owner's id
     * @param itemId item's id
     * @return item
     */
    ItemResponseDto getById(Long userId, Long itemId);

    /**
     * update item's properties
     *
     * @param userId  owner's id
     * @param itemId  item's id
     * @param itemDto ItemDto object with properties to update
     * @return updated item
     */
    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    /**
     * delete item by id
     *
     * @param itemId item's id
     */
    void deleteById(Long itemId);

    /**
     * get all items of a specific user
     *
     * @param userId user's id
     * @return list of items
     */
    List<ItemResponseDto> getListByUser(Long userId);

    /**
     * search all available items, contained substring in name or description
     *
     * @param text substring for search
     * @return list of items or empty list
     */
    List<ItemResponseDto> searchItemsBySubstring(String text);

    /**
     * add comment to a specific item
     *
     * @param commentRequestDto comment
     * @param userId            author's id
     * @param itemId            item's id
     * @return comment
     */

    CommentResponseDto addComment(CommentRequestDto commentRequestDto, Long userId, Long itemId);

}
