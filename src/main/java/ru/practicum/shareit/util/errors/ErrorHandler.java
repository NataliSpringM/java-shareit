package ru.practicum.shareit.util.errors;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.util.exceptions.ConflictEmailException;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

/**
 * Exceptions handling
 */
@RestControllerAdvice("ru.practicum.shareit")
public class ErrorHandler {

    /**
     * Handle errors with non-registered in storages id
     *
     * @param e Exception
     * @return ErrorResponse
     */
    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundId(final RuntimeException e) {

        return new ErrorResponse("Несуществующий id: " + e.getMessage());
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
            ConstraintViolationException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleFailValidation(final RuntimeException e) {

        return new ErrorResponse("Ошибка валидации: " + e.getMessage());
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

