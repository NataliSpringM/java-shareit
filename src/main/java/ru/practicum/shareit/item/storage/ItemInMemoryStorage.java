package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ItemStorage in memory implementation
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ItemInMemoryStorage implements ItemStorage {

    private final Map<Long, Item> items;
    private Long nextId = 1L;

    /**
     * create Item
     *
     * @param item ItemDto object for creating
     * @return registered ItemDto object
     */
    @Override
    public Item create(Item item) {
        Long itemId = generateId();
        Item itemWithId = item.toBuilder().id(itemId).build();
        save(itemWithId);
        log.info("Зарегистрирована вещь: {}", itemWithId);
        return itemWithId;
    }

    /**
     * get Item object
     *
     * @param itemId item's id
     * @return Item object
     */
    @Override
    public Item getById(Long itemId) {
        if (items.containsKey(itemId)) {
            Item item = items.get(itemId);
            log.info("Вещь с id: {} найдена, {}", itemId, item);
            return item;
        } else {
            throw new ObjectNotFoundException(String.format("Вещь с id %d не найдена", itemId));
        }
    }


    /**
     * update Item object
     *
     * @param item Item object with properties to update
     * @return updated ItemDto object
     */
    @Override
    public Item update(Item item) {

        items.put(item.getId(), item);
        log.info("Обновлена информация о вещи {}", item);
        return item;
    }

    /**
     * delete Item object
     *
     * @param itemId item's id
     * @return if delete operation was executed
     */
    @Override
    public boolean delete(Long itemId) {
        if (items.containsKey(itemId)) {
            items.remove(itemId);
            log.info("Удалена информация о вещи с id: {}", itemId);
            return true;
        }
        return false;
    }

    /**
     * search all available items, contained substring in name or description
     *
     * @param substring substring for search
     * @return list of ItemDto objects
     */
    @Override
    public List<ItemDto> searchItemsBySubstring(String substring) {
        return ItemMapper.toItemDtoList(items.values().stream()
                .filter(item -> (item.getName().toLowerCase().contains(substring.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(substring.toLowerCase())) &&
                        item.getAvailable())
                .collect(Collectors.toList()));
    }

    /**
     * get all items
     *
     * @return list of Item objects
     */
    @Override
    public List<Item> getList() {
        List<Item> list = new ArrayList<>(items.values());
        logResultList(list);
        return list;
    }

    /**
     * checks if item is registered in Storage
     *
     * @param itemId item's id
     */
    @Override
    public void checkItemIdExists(Long itemId) {
        if (!items.containsKey(itemId)) {
            log.info("Ошибка при проверке id вещи, id: {} ", itemId);
        }
    }

    /**
     * generate next id
     *
     * @return next id
     */
    private Long generateId() {
        return nextId++;
    }

    /**
     * save new or updated Object into map for storage
     *
     * @param item Item object
     */
    private void save(Item item) {
        items.put(item.getId(), item);
    }

    /**
     * log list of objects in pretty format
     *
     * @param users list of users
     */
    private void logResultList(List<Item> users) {
        String result = users.stream()
                .map(Item::toString)
                .collect(Collectors.joining(", "));
        log.info("Список вещей по запросу: {}", result);

    }

}
