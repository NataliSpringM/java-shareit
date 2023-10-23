package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Sprint add-bookings.
 * BookingItemResponse DTO model for properties lastBooking and nextBooking in ItemResponseDto model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class BookingItemResponseDto {
    Long id;
    Long bookerId;
}
