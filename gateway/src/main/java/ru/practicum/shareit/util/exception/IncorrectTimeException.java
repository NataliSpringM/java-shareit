package ru.practicum.shareit.util.exception;

/**
 * Exception - incorrect start / end time for booking are stated
 */
public class IncorrectTimeException extends RuntimeException {
    public IncorrectTimeException(String message) {
        super(message);
    }

}

