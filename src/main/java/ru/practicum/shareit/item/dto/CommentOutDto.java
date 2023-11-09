package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Sprint add-bookings.
 * CommentOutDto model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class CommentOutDto {
    Long id;
    String text;
    String authorName;
    Long itemId;
    LocalDateTime created;

}
