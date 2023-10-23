package ru.practicum.shareit.booking.model;

/**
 * Enumeration of BookingStatus (status of approving by the owner of the item).
 */
public enum BookingStatus {
    /**
     * WAITING - bookings, waiting for approving or declining from the item's owner
     */
    WAITING,
    /**
     * APPROVED - approved by item's owner bookings
     */
    APPROVED,
    /**
     * REJECTED - rejected by item's owner bookings
     */
    REJECTED,
    /**
     * CANCELLED - cancelled bookings
     */
    CANCELED
}
