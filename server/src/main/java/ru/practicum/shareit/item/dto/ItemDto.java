package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Item DTO model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class ItemDto {

    Long id;
    String name;
    String description;
    Boolean available;
    Long requestId;

}
