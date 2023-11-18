package ru.practicum.shareit.util.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.util.exception.IncorrectTimeException;

import javax.validation.ConstraintViolationException;

/**
 * Exceptions handling
 */
@RestControllerAdvice("ru.practicum.shareit")
public class ErrorHandler {

    /**
     * Handle errors with invalid input data
     *
     * @param e Exception
     * @return ErrorResponse
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            IncorrectTimeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleFailValidation(final RuntimeException e) {
        return new ErrorResponse("Ошибка валидации:" + e.getMessage() + ", " + e.getCause());
    }

    /**
     * Handle errors with invalid input data
     *
     * @param e Exception
     * @return ErrorResponse
     */
    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleFaiStatuslValidation(final RuntimeException e) {
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

