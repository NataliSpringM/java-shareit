package ru.practicum.shareit.item.mapper;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingItemResponseDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
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
     * map ItemDto object into ItemResponseDto object to test
     *
     * @param itemDto ItemDto object
     * @return ItemResponseDto object with null empty properties
     */
    public static ItemResponseDto toItemResponseDto(ItemDto itemDto) {
        return new ItemResponseDto(
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
     * map Item object into ItemResponseDto object
     *
     * @param item        Item object
     * @param lastBooking lastBooking of item
     * @param nextBooking nextBooking of item
     * @return ItemResponseDto object with null empty properties
     */
    public static ItemResponseDto toItemResponseDto(Item item,
                                                    BookingItemResponseDto lastBooking,
                                                    BookingItemResponseDto nextBooking,
                                                    List<CommentResponseDto> comments) {
        return new ItemResponseDto(
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

    public static Item toItem(ItemDto itemDto, User user, @Nullable ItemRequest request) {
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
