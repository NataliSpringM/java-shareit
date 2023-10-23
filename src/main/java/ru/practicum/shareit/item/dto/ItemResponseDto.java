package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import ru.practicum.shareit.booking.dto.BookingItemResponseDto;
import ru.practicum.shareit.util.groups.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Sprint add-bookings.
 * ItemResponseDto model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class ItemResponseDto {
    Long id;
    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    String name;
    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    String description;
    @NotNull(groups = {Create.class})
    Boolean available;
    BookingItemResponseDto lastBooking;
    BookingItemResponseDto nextBooking;
    Long requestId;
    List<CommentResponseDto> comments;
}
