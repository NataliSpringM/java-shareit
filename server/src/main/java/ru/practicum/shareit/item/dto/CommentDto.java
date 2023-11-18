package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * CommentDto model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class CommentDto {
    Long id;
    String text;
    String authorName;
    Long itemId;

}
