package ru.practicum.shareit.request;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
@Validated
public class ItemRequest {

    Long id;
    String description;
    Long requestor;
    LocalDateTime created;

}