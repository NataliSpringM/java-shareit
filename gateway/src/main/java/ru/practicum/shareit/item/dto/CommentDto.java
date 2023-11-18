package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.util.groups.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * CommentDto model.
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CommentDto {
    private Long id;
    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    private String text;
    private String authorName;
    private Long itemId;

}
