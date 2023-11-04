package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Value;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Sprint add-item-requests.
 * ItemRequest DTO model.
 */
@Builder(toBuilder = true)
@Value
@RequiredArgsConstructor
public class ItemRequestDto {

    Long id;
    @NotNull
    @NotBlank
    String description;
}
