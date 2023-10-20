package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

/**
 * BookingService interface
 */
public interface BookingService {
    /**
     * to add booking (save and assign identity)
     *
     * @param userId            owner's id
     * @param bookingRequestDto booking to save and register
     * @return booking with assigned id
     */
    BookingResponseDto create(Long userId, BookingRequestDto bookingRequestDto);

    /**
     * get booking by booking's id for owner or booker
     *
     * @param userId    user's id
     * @param bookingId booking's id
     * @return booking
     */
    BookingResponseDto getById(Long userId, Long bookingId);

    /**
     * set APPROVE or REJECTED BookingStatus for booking. Updating of status is allowed only for item's owner
     *
     * @param bookingId booking's id
     * @param userId    user's id
     * @param approved  boolean
     * @return booking with updated (APPROVED or REJECTED status)
     */
    BookingResponseDto updateStatus(Long bookingId, Long userId, Boolean approved);

    /**
     * find if exists list of booking by owner's id, sorting by start value, starting with new
     * by state (default = ALL)
     *
     * @param ownerId owner's id
     * @return list of bookings of items' owner according to specified criteria, sorting by start in descending order
     */
    List<BookingResponseDto> getListByOwner(Long ownerId, String state);

    /**
     * find if exists list of booking by user's id, sorting by start value, starting with new
     * by state (default = ALL)
     *
     * @param bookerId user's id
     * @return list of bookings of a specific user according to specified criteria, sorting by start in descending order
     */
    List<BookingResponseDto> getListByBooker(Long bookerId, String state);
}
