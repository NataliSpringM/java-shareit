package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.util.groups.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Sprint add-bookings.
 * ItemOutDto model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class ItemOutDto {
    Long id;
    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    String name;
    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    String description;
    @NotNull(groups = {Create.class})
    Boolean available;
    BookingItemDto lastBooking;
    BookingItemDto nextBooking;
    Long requestId;
    List<CommentOutDto> comments;
}
