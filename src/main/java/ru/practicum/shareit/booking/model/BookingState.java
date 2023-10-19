package ru.practicum.shareit.booking.model;

/**
 * Enumeration of valid BookingStates for requests.
 */
public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED
}