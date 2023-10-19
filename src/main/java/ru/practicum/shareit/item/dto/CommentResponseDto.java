package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Sprint add-bookings.
 * CommentResponseDto model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class CommentResponseDto {
    Long id;
    @NotNull
    @NotBlank
    String text;
    String authorName;
    Long itemId;
    LocalDateTime created;

}
