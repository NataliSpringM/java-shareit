package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * ItemRequest DTO model.
 */
@Builder(toBuilder = true)
@Value
@RequiredArgsConstructor
public class ItemRequestDto {

    Long id;
    String description;
}
