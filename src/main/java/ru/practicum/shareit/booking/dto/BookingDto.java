package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import ru.practicum.shareit.util.groups.Create;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Sprint add-bookings.
 * BookingRequest DTO model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class BookingDto {
    Long id;
    @FutureOrPresent(groups = {Create.class})
    @NotNull(groups = {Create.class})
    LocalDateTime start;
    @Future(groups = {Create.class})
    @NotNull(groups = {Create.class})
    LocalDateTime end;
    Long itemId;

}
