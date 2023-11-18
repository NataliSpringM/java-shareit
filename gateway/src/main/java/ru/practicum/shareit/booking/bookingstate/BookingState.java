package ru.practicum.shareit.booking.bookingstate;

/**
 * Enumeration of valid BookingStates for requests.
 */
public enum BookingState {
    /**
     * ALL - all bookings
     */
    ALL,
    /**
     * CURRENT - current bookings
     */
    CURRENT,
    /**
     * PAST - past bookings
     */
    PAST,
    /**
     * FUTURE - future bookings
     */
    FUTURE,
    /**
     * WAITING - new bookings, waiting for approve or reject from the item's owner
     */
    WAITING,
    /**
     * REJECTED - rejected by item's owner bookings
     */
    REJECTED
}

