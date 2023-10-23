package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
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
     * @return Comment Dto object
     */
    public static CommentResponseDto toCommentResponseDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getName() : null)
                .itemId(comment.getItem() != null ? comment.getItem().getId() : null)
                .created(comment.getCreated())
                .build();

    }

    /**
     * map CommentRequestDto object into Comment object
     *
     * @param commentRequestDto CommentRequestDto object
     * @param item              Item object
     * @param user              User object
     * @return Comment object
     */
    public static Comment toComment(CommentRequestDto commentRequestDto, User user, Item item) {
        return Comment.builder()
                .id(commentRequestDto.getId())
                .text(commentRequestDto.getText())
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

    public static List<CommentResponseDto> toCommentResponseDtoList(List<Comment> comments) {
        return comments.stream().map(CommentMapper::toCommentResponseDto).collect(Collectors.toList());
    }

}
