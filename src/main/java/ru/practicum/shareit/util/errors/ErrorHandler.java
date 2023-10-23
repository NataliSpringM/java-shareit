package ru.practicum.shareit.util.errors;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.util.exceptions.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

/**
 * Exceptions handling
 */
@RestControllerAdvice("ru.practicum.shareit")
public class ErrorHandler {

    /**
     * Handle errors with not found objects or objects not available for modification
     *
     * @param e Exception
     * @return ErrorResponse
     */
    @ExceptionHandler({ObjectNotFoundException.class, EntityNotFoundException.class,
            AccessIsNotAllowedException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundId(final RuntimeException e) {

        return new ErrorResponse("Объект по запросу не найден: " + e.getMessage());
    }

    /**
     * Handle errors with email already registered by another user
     *
     * @param e Exception
     * @return ErrorResponse
     */
    @ExceptionHandler(ConflictEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateEmail(final RuntimeException e) {

        return new ErrorResponse("Попытка использовать зарегистрированный email: " + e.getMessage());
    }


    /**
     * Handle errors with invalid input data
     *
     * @param e Exception
     * @return ErrorResponse
     */
    @ExceptionHandler({ValidationException.class,
            ConstraintViolationException.class, MethodArgumentNotValidException.class,
            IncorrectTimeException.class, UnavailableItemException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleFailValidation(final RuntimeException e) {
        return new ErrorResponse("Ошибка валидации: " + e.getMessage());
    }

    @ExceptionHandler({UnsupportedStatusException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnsupportedBookingStatus(final RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }


    /**
     * Handle all unknown errors
     *
     * @param e Exception
     * @return ErrorResponse
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnknownError(final Throwable e) {

        return new ErrorResponse("Произошла непредвиденная ошибка: " + e.getMessage());
    }

}

