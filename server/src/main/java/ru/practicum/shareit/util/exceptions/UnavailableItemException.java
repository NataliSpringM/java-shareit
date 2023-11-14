package ru.practicum.shareit.util.exceptions;

/**
 * Exception - item is unavailable
 */
public class UnavailableItemException extends RuntimeException {

    public UnavailableItemException(String message) {
        super(message);
    }
}
