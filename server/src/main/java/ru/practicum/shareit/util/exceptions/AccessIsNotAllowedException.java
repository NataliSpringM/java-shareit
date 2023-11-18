package ru.practicum.shareit.util.exceptions;

public class AccessIsNotAllowedException extends RuntimeException {
    public AccessIsNotAllowedException(String message) {
        super(message);
    }
}
