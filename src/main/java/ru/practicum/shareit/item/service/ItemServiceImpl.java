package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.util.Validation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ItemService implementation
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    /**
     * create ItemDto
     *
     * @param userId  owner's id
     * @param itemDto ItemDto object to register
     * @return registered ItemDto object
     */
    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        userStorage.checkUserIdExists(userId);
        User owner = userStorage.getById(userId);
        Item item = ItemMapper.toItem(itemDto, owner, null);
        Item itemWithId = itemStorage.create(item);
        userStorage.saveItem(userId, itemWithId.getId());
        return ItemMapper.toItemDto(itemWithId);
    }

    /**
     * get ItemDto object
     *
     * @param userId owner's id
     * @param itemId item's id
     * @return ItemDto object
     */
    @Override
    public ItemDto getById(Long userId, Long itemId) {
        Item item = itemStorage.getById(itemId);
        return ItemMapper.toItemDto(item);
    }

    /**
     * update ItemDto object
     *
     * @param userId  owner's id
     * @param itemId  item's id
     * @param itemDto ItemDto object with properties to update
     * @return updated ItemDto object
     */
    @Override
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {
        userStorage.checkUserIdExists(userId);
        userStorage.checkOwner(userId, itemId);
        Item newItem = updateValidFields(itemDto, itemId);
        Item updatedItem = itemStorage.update(newItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    /**
     * delete ItemDto object
     *
     * @param itemId item's id
     */
    @Override
    public void delete(Long itemId) {
        itemStorage.checkItemIdExists(itemId);
        userStorage.deleteItem(itemId);
        itemStorage.delete(itemId);
    }

    /**
     * get all items of a specific user
     *
     * @param userId user's id
     * @return list of ItemDto objects
     */
    @Override
    public List<ItemDto> getListByUser(Long userId) {
        List<Long> itemsId = userStorage.getItemsId(userId);
        List<Item> items = itemsId.stream().map(itemStorage::getById).collect(Collectors.toList());
        return ItemMapper.toItemDtoList(items);
    }

    /**
     * search all available items, contained substring in name or description
     *
     * @param substring substring for search
     * @return list of ItemDto objects
     */
    @Override
    public List<ItemDto> searchItemsBySubstring(String substring) {
        if (substring == null || substring.isBlank()) {
            return Collections.emptyList();
        }
        return itemStorage.searchItemsBySubstring(substring);
    }

    /**
     * update valid fields
     *
     * @param itemDto properties to check and update
     * @param itemId  item's id
     * @return updated Item object
     */
    private Item updateValidFields(ItemDto itemDto, Long itemId) {

        Item item = itemStorage.getById(itemId);

        String newName = itemDto.getName();
        String newDescription = itemDto.getDescription();
        Boolean newStatus = itemDto.getAvailable();

        if (Validation.stringIsNotNullOrBlank(newName)) {
            item = item.toBuilder().name(newName).build();
        }
        if (Validation.stringIsNotNullOrBlank(newDescription)) {
            item = item.toBuilder().description(newDescription).build();
        }
        if (Validation.ObjectIsNotNull(newStatus)) {
            item = item.toBuilder().available(newStatus).build();
        }
        return item;
    }
}
