package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Value;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sprint add-item-requests.
 * ItemRequest DTO model.
 */
@Builder(toBuilder = true)
@Value
@RequiredArgsConstructor
public class ItemRequestOutDto {

    Long id;
    String description;
    LocalDateTime created;
    List<ItemDto> items;
}
