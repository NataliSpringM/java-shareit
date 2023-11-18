package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import ru.practicum.shareit.booking.dto.BookingItemDto;

import java.util.List;

/**
 * ItemOutDto model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class ItemOutDto {
    Long id;
    String name;
    String description;
    Boolean available;
    BookingItemDto lastBooking;
    BookingItemDto nextBooking;
    Long requestId;
    List<CommentOutDto> comments;
}
