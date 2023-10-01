package ru.practicum.shareit.util.errors;

import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * ErrorResponse
 */
@Value
@RequiredArgsConstructor
public class ErrorResponse {

    String error;

}