package ru.practicum.shareit.util.exceptions;

/**
 * Exception object not found in storage by invalid id
 */
public class ObjectNotFoundException extends RuntimeException {

    public ObjectNotFoundException(String message) {
        super(message);
    }

}