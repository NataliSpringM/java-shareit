package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import ru.practicum.shareit.util.groups.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Sprint add-bookings.
 * CommentDto model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class CommentDto {
    Long id;
    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    String text;
    String authorName;
    Long itemId;

}
