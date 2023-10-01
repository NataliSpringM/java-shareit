package ru.practicum.shareit.util.exceptions;

/**
 * Exception - email has already registered by another user
 */
public class ConflictEmailException extends RuntimeException {
    public ConflictEmailException(String message) {
        super(message);
    }
}
