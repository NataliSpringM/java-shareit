package ru.practicum.shareit.booking.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingItemResponseDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {
    /**
     * map Booking object into BookingResponseDto object
     *
     * @param booking Booking object
     * @return BookingResponseDto object
     */
    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                booking.getBooker(),
                booking.getItem()
        );
    }

    /**
     * map Booking object into BookingItemResponseDto object
     *
     * @param booking Booking object
     * @return BookingItemResponseDto object
     */
    public static BookingItemResponseDto toBookingItemResponseDto(Booking booking) {
        return new BookingItemResponseDto(
                booking.getId(),
                booking.getBooker().getId()
        );
    }

    /**
     * map BookingRequestDto object into Booking object
     *
     * @param bookingRequestDto BookingRequestDto object
     * @return Booking object
     */
    public static Booking toBooking(BookingRequestDto bookingRequestDto,
                                    User user, Item item, BookingStatus status) {
        return Booking.builder()
                .id(bookingRequestDto.getId())
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .booker(user)
                .item(item)
                .status(status)
                .build();
    }

    /**
     * map List of Booking objects into List of BookingResponseDto objects
     *
     * @param bookings list of Booking objects
     * @return List of BookingResponseDto objects
     */

    public static List<BookingResponseDto> toBookingResponseDtoList(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    /**
     * map BookingResponse object into BookingItemResponseDto object to test
     *
     * @param bookingResponseDto BookingResponseDto object
     * @return BookingItemResponseDto object
     */
    public static BookingItemResponseDto toBookingItemResponseDto(BookingResponseDto bookingResponseDto) {
        return new BookingItemResponseDto(
                bookingResponseDto.getId(),
                bookingResponseDto.getBooker().getId()
        );
    }

}

