package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.dto.CommentOutDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;


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
     * map ItemDto object into ItemOutDto object to test
     *
     * @param itemDto ItemDto object
     * @return ItemOutDto object with null empty properties
     */
    public static ItemOutDto toItemOutDto(ItemDto itemDto) {
        return new ItemOutDto(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                null,
                null,
                null,
                Collections.emptyList()
        );
    }

    /**
     * map Item object into ItemOutDto object
     *
     * @param item        Item object
     * @param lastBooking lastBooking of item
     * @param nextBooking nextBooking of item
     * @return ItemOutDto object with null empty properties
     */
    public static ItemOutDto toItemOutDto(Item item,
                                          BookingItemDto lastBooking,
                                          BookingItemDto nextBooking,
                                          List<CommentOutDto> comments) {
        return new ItemOutDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBooking,
                nextBooking,
                item.getRequest() != null ? item.getRequest().getId() : null,
                comments
        );
    }


    /**
     * map ItemDto object into Item object
     *
     * @param itemDto ItemDto object
     * @return Item object
     */

    public static Item toItem(ItemDto itemDto, User user, ItemRequest request) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(user)
                .request(request)
                .build();
    }

}
