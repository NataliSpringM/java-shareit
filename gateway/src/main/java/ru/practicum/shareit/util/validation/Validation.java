package ru.practicum.shareit.util.validation;

import ru.practicum.shareit.booking.bookingstate.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.util.exception.IncorrectTimeException;

import java.time.LocalDateTime;
import java.util.Arrays;

public class Validation {

    /**
     * check whether start and end time of Booking are valid
     * throws 400.BAD_REQUEST IncorrectTimeException if aren't
     * start time should not equal end time, end time should not be before start time
     *
     * @param bookingDto request to book
     */
    public static void checkStartIsNotEqualsOrBeforeEnd(BookingDto bookingDto) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();

        if (start.equals(end) || start.isAfter(end)) {
            throw new IncorrectTimeException("Указаны некорректные даты начала и/или конца бронирования");
        }
    }

    /**
     * check whether string is valid bookingState
     * throws 400.BAD_REQUEST UnsupportedStatusException if isn't
     * @param state string to check
     */
    public static BookingState getBookingStateIfValid(String state) {
        return Arrays.stream(BookingState.values())
                .filter(value -> value.name().equalsIgnoreCase(state))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
    }

  /*

    private BookingState getValidBookingStateOrElseThrow(String state) {

        BookingState validState;
        try {
            validState = BookingState.valueOf(state);
        } catch (IllegalArgumentException exception) {
            throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        return validState;
    }
*/
}

