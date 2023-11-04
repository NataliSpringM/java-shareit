package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentOutDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * map Comment, CommentDto objects into each other
 */
@Component
public class CommentMapper {

    /**
     * map Comment object into CommentDto object
     *
     * @param comment Comment object
     * @return CommentOutDto object
     */
    public static CommentOutDto toCommentOutDto(Comment comment) {
        return CommentOutDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getName() : null)
                .itemId(comment.getItem() != null ? comment.getItem().getId() : null)
                .created(comment.getCreated())
                .build();

    }

    /**
     * map Comment object into CommentDto object to test
     *
     * @param commentDto CommentDto object
     * @return CommentOutDto object
     */
    public static CommentOutDto toCommentOutDto(CommentDto commentDto) {
        return CommentOutDto.builder()
                .id(commentDto.getId())
                .text(commentDto.getText())
                .authorName(commentDto.getAuthorName())
                .itemId(commentDto.getItemId())
                .created(LocalDateTime.now())
                .build();

    }

    /**
     * map CommentDto object into Comment object
     *
     * @param commentDto CommentDto object
     * @param item       Item object
     * @param user       User object
     * @return Comment object
     */
    public static Comment toComment(CommentDto commentDto, User user, Item item) {
        return Comment.builder()
                .id(commentDto.getId())
                .text(commentDto.getText())
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();
    }

    /**
     * map List of Comment objects into List of CommentDto objects
     *
     * @param comments list of Comment objects
     * @return List of CommentDto objects
     */

    public static List<CommentOutDto> toCommentOutDtoList(List<Comment> comments) {
        return comments.stream().map(CommentMapper::toCommentOutDto).collect(Collectors.toList());
    }

}
