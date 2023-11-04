package ru.practicum.shareit.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ItemRequestMapper {

    /**
     * map ItemRequest object into ItemRequestOutDto object
     *
     * @param itemRequest ItemRequest object
     * @param items       list of the items offered according to request
     * @return ItemRequestOutDto object
     */

    public static ItemRequestOutDto toItemRequestOutDto(ItemRequest itemRequest, List<ItemDto> items) {
        return new ItemRequestOutDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                items == null ? new ArrayList<>() : items
        );
    }

    /**
     * map ItemRequestDto object into Item object
     *
     * @param itemRequestDto ItemRequestDto object
     * @param user           requester
     * @return ItemRequest object
     */
    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .created(LocalDateTime.now())
                .requester(user)
                .build();
    }

    /**
     * map ItemRequestOutDto object into Item object
     *
     * @param itemRequestOutDto ItemRequestOutDto object
     * @param user              requester
     * @return ItemRequest object
     */
    public static ItemRequest toItemRequest(ItemRequestOutDto itemRequestOutDto, User user) {
        return ItemRequest.builder()
                .description(itemRequestOutDto.getDescription())
                .created(itemRequestOutDto.getCreated())
                .requester(user)
                .build();
    }


}
