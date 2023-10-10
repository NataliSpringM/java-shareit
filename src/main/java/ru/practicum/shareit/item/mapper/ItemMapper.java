package ru.practicum.shareit.item.mapper;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * map Item, ItemDto objects into each other
 */
@Component
public class ItemMapper {
    /**
     * map Item object into IteDto object
     *
     * @param item Item object
     * @return ItemDto object
     */
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    /**
     * map ItemDto object into Item object
     *
     * @param item ItemDto object
     * @return Item object
     */

    public static Item toItem(ItemDto item, User user, @Nullable ItemRequest request) {
        return new Item(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                user,
                request
        );
    }

    /**
     * map List of Item objects into List of ItemDto objects
     *
     * @param items list of Item objects
     * @return List of ItemDto objects
     */

    public static List<ItemDto> toItemDtoList(List<Item> items) {
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

}
