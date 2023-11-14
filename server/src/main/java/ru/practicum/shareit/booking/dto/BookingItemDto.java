package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Sprint add-bookings.
 * BookingForItem DTO model for properties lastBooking and nextBooking in ItemOutDto model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class BookingItemDto {
    Long id;
    Long bookerId;
}
