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
     * create ItemDto
     *
     * @param userId  owner's id
     * @param itemDto ItemDto object to register
     * @return registered ItemDto object
     */
    ItemDto create(Long userId, @Valid ItemDto itemDto);

    /**
     * get ItemDto object
     *
     * @param userId owner's id
     * @param itemId item's id
     * @return ItemDto object
     */
    ItemResponseDto getById(Long userId, Long itemId);

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
     * delete Item object by id
     *
     * @param itemId item's id
     */
    void deleteById(Long itemId);

    /**
     * get all items of a specific user
     *
     * @param userId user's id
     * @return list of ItemDto objects
     */
    List<ItemResponseDto> getListByUser(Long userId);

    /**
     * search all available items, contained substring in name or description
     *
     * @param text substring for search
     * @return list of ItemDto objects or empty list
     */
    List<ItemResponseDto> searchItemsBySubstring(String text);

    /**
     * add comment to a specific item
     *
     * @param commentRequestDto CommentRequestDto object
     * @param userId            author's id
     * @param itemId            item's id
     * @return CommentResponseDto object
     */

    CommentResponseDto addComment(CommentRequestDto commentRequestDto, Long userId, Long itemId);

}
