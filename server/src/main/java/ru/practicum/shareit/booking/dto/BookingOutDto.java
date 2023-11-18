package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * Sprint add-bookings.
 * BookingItemResponse DTO model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class BookingOutDto {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    BookingStatus status;
    User booker;
    Item item;

}
