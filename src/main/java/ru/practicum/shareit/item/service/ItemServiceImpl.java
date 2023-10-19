package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingItemResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentJpaRepository;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;
import ru.practicum.shareit.util.Validation;
import ru.practicum.shareit.util.exceptions.AccessIsNotAllowedException;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.util.exceptions.UnavailableItemException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ItemService implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final UserJpaRepository userJpaRepository;
    private final ItemJpaRepository itemJpaRepository;
    private final BookingJpaRepository bookingJpaRepository;
    private final CommentJpaRepository commentJpaRepository;

    /**
     * create ItemDto
     * throws 404.NOT_FOUND ObjectNotFoundException if user doesn't exist
     *
     * @param userId  owner's id
     * @param itemDto ItemDto object to register
     * @return registered ItemDto object
     */
    @Override
    @Transactional
    public ItemDto create(Long userId, @Valid ItemDto itemDto) {
        User owner = getUserByIdIfExists(userId);

        Item item = ItemMapper.toItem(itemDto, owner, null);
        Item itemWithId = itemJpaRepository.save(item);
        log.info("Зарегистрирована вещь: {}", itemWithId);
        return ItemMapper.toItemDto(itemWithId);
    }

    /**
     * get ItemResponseDto object with lastBooking, nextBooking and comments
     * throws 404.NOT_FOUND ObjectNotFoundException if item doesn't exist
     *
     * @param userId owner's id
     * @param itemId item's id
     * @return ItemDto object
     */
    @Override
    @Transactional(readOnly = true)
    public ItemResponseDto getById(Long userId, Long itemId) {

        Item item = getItemByIdIfExists(itemId);

        LocalDateTime now = LocalDateTime.now();
        BookingItemResponseDto lastBooking = null;
        BookingItemResponseDto nextBooking = null;
        if (item.getOwner().getId().equals(userId)) {
            lastBooking = getLastBooking(itemId, now);
            nextBooking = getNextBooking(itemId, now);
        }

        List<CommentResponseDto> commentsDto = getCommentsByItemId(itemId);
        ItemResponseDto itemResponseDto = ItemMapper.toItemResponseDto(item, lastBooking, nextBooking, commentsDto);
        log.info("Вещь с id: {} найдена по запросу пользователя с id {}, {}", itemId, userId, itemResponseDto);
        return itemResponseDto;
    }


    /**
     * update ItemDto object
     * throws 404.NOT_FOUND ObjectNotFoundException if user doesn't exist
     * throws 404.NOT_FOUND ObjectNotFoundException if item doesn't exist
     * throws 404.NOT_FOUND AccessIsNotAllowedException if user trying updating is not item's owner
     *
     * @param userId  owner's id
     * @param itemId  item's id
     * @param itemDto ItemDto object with properties to update
     * @return updated ItemDto object
     */
    @Override
    @Transactional
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {

        checkUserExists(userId);
        Item item = getItemByIdIfExists(itemId);
        checkAccessAllowedOnlyForOwner(item, userId);

        Item updatedItem = updateValidFields(item, itemDto);

        itemJpaRepository.save(updatedItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    /**
     * delete ItemDto object by id
     *
     * @param itemId item's id
     */
    @Override
    @Transactional
    public void deleteById(Long itemId) {
        if (itemJpaRepository.existsById(itemId)) {
            log.info("Удалена вещь с id: {}", itemId);
            itemJpaRepository.deleteById(itemId);
        }
        log.info("Вещи с id: {} не существует", itemId);
    }

    /**
     * get all items of a specific user
     *
     * @param userId user's id
     * @return list of ItemDto objects
     */
    @Override
    @Transactional(readOnly = true)
    public List<ItemResponseDto> getListByUser(Long userId) {

        List<Item> items = itemJpaRepository.findAllByOwnerId(userId);
        Map<Item, List<Comment>> mapComments = getCommentsToAllItems(items);
        List<ItemResponseDto> itemsResponses = items.stream()
                .map(item -> getItemResponseDto(item,
                        mapComments.getOrDefault(item, Collections.emptyList()),
                        LocalDateTime.now()))
                .collect(Collectors.toList());
        logResultList(itemsResponses);
        return itemsResponses;
    }

    /**
     * search all available items, contained substring in name or description
     *
     * @param text substring for search
     * @return list of ItemDto objects
     */
    @Override
    @Transactional(readOnly = true)
    public List<ItemResponseDto> searchItemsBySubstring(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = itemJpaRepository.searchItemsBySubstring(text);
        Map<Item, List<Comment>> mapComments = getCommentsToAllItems(items);
        List<ItemResponseDto> itemsResponses = items.stream()
                .map(item -> getItemResponseDto(item,
                        mapComments.getOrDefault(item, Collections.emptyList())
                        , LocalDateTime.now()))
                .collect(Collectors.toList());

        logResultList(itemsResponses);
        return itemsResponses;
    }

    /**
     * add comment to a specific item
     * throws 404.NOT_FOUND ObjectNotFoundException if user doesn't exist
     * throws 404.NOT_FOUND ObjectNotFoundException if item doesn't exist
     * throws 404.NOT_FOUND AccessIsNotAllowedException if owner tries to comment
     * throws 400.BAD_REQUEST UnavailableItemException if user hadn't approved item's booking before
     *
     * @param commentRequestDto CommentRequestDto object
     * @param userId            author's id
     * @param itemId            item's id
     * @return CommentResponseDto object
     */
    @Override
    @Transactional
    public CommentResponseDto addComment(CommentRequestDto commentRequestDto, Long userId, Long itemId) {

        Item item = getItemByIdIfExists(itemId);
        User user = getUserByIdIfExists(userId);
        checkAccessForOwnerNotAllowed(item, userId);
        checkAccessToCommentAllowed(userId, itemId);

        Comment comment = CommentMapper.toComment(commentRequestDto, user, item);
        commentJpaRepository.save(comment);
        log.info("Для вещи c id {} пользователь id {} добавил новый отзыв: {}", itemId, userId, comment);
        return CommentMapper.toCommentResponseDto(comment);
    }

    /**
     * update valid fields
     *
     * @param itemDto properties to check and update
     * @param item    Item to update
     * @return updated Item object
     */
    private Item updateValidFields(Item item, ItemDto itemDto) {

        String newName = itemDto.getName();
        String newDescription = itemDto.getDescription();
        Boolean newStatus = itemDto.getAvailable();

        if (Validation.stringIsNotNullOrBlank(newName)) {
            item = item.toBuilder().name(newName).build();
        }
        if (Validation.stringIsNotNullOrBlank(newDescription)) {
            item = item.toBuilder().description(newDescription).build();
        }
        if (Validation.objectIsNotNull(newStatus)) {
            item = item.toBuilder().available(newStatus).build();
        }
        return item;
    }

    /**
     * get User if exists throw 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param userId user's id
     * @return User object
     */
    private User getUserByIdIfExists(Long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(String.format("Пользователя с id %d не существует", userId)));
    }

    /**
     * get Item if exists throw 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param itemId item's id
     * @return Item object
     */
    private Item getItemByIdIfExists(Long itemId) {
        return itemJpaRepository.findById(itemId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(String.format("Вещи с id %d не существует", itemId)));
    }

    /**
     * check whether User exists throw 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param userId user's id
     */
    private void checkUserExists(Long userId) {
        if (!userJpaRepository.existsById(userId)) {
            throw new ObjectNotFoundException(String.format("Пользователя с id %d не существует", userId));
        }
    }


    /**
     * check whether user had item's approved bookings, throws 400.BAD_REQUEST UnavailableItemException if hadn't
     *
     * @param itemId item's id
     * @param userId user's id
     */

    private void checkAccessToCommentAllowed(Long userId, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingJpaRepository.findAllByItem_IdAndBooker_IdAndStatusAndStartIsBefore
                (itemId, userId, BookingStatus.APPROVED, now);

        if (bookings.isEmpty()) {
            throw new UnavailableItemException("Вы не вправе оставлять отзывы, "
                    + "поскольку не пользовались данной вещью");
        }
    }


    /**
     * check whether user is not item's owner, throws 404.NOT_FOUND AccessIsNotAllowedException if is
     *
     * @param item   Item item
     * @param userId user's id
     */
    private void checkAccessForOwnerNotAllowed(Item item, Long userId) {
        if (isOwner(item, userId)) {
            throw new AccessIsNotAllowedException(
                    "Объект не найден среди доступных для бронирования: владелец не может забронировать свою вещь.");
        }
    }

    /**
     * check whether user is item's owner, throws 404.NOT_FOUND AccessIsNotAllowedException if isn't
     *
     * @param item   Item item
     * @param userId user's id
     */
    private void checkAccessAllowedOnlyForOwner(Item item, Long userId) {

        if (!isOwner(item, userId)) {
            throw new AccessIsNotAllowedException(
                    (String.format("Операция доступна только владельцу вещи %s :", item)));
        }
    }


    /**
     * check whether user is item's owner
     *
     * @param item   Item item
     * @param userId user's id
     * @return boolean
     */
    private boolean isOwner(Item item, Long userId) {
        return item.getOwner().getId().equals(userId);
    }

    /**
     * construct and get ItemResponseDto by current time
     *
     * @param item Item
     * @param now  current time
     * @return ItemResponseDto object
     */
    private ItemResponseDto getItemResponseDto(Item item, List<Comment> comments, LocalDateTime now) {

        Long itemId = item.getId();
        BookingItemResponseDto lastBooking = getLastBooking(itemId, now);
        BookingItemResponseDto nextBooking = getNextBooking(itemId, now);
        List<CommentResponseDto> commentsDto = CommentMapper.toCommentResponseDtoList(comments);

        return ItemMapper.toItemResponseDto(item, lastBooking, nextBooking, commentsDto);
    }

    /**
     * get map ItemId:List of comments - map of comments grouping by items
     *
     * @param items list of items
     * @return map of comments grouping by items
     */
    private Map<Item, List<Comment>> getCommentsToAllItems(List<Item> items) {

        List<Comment> comments = commentJpaRepository.findAllByItemIn(items);
        return comments.stream()
                .collect(Collectors.groupingBy(Comment::getItem));
    }

    /**
     * get item's lastBooking relating to a specified time
     *
     * @param itemId item's id
     * @param now    current time
     * @return BookingItemResponseDto object or null result
     */
    private BookingItemResponseDto getLastBooking(Long itemId, LocalDateTime now) {
        return bookingJpaRepository
                .findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc
                        (itemId, BookingStatus.APPROVED, now, now)
                .map(BookingMapper::toBookingItemResponseDto)
                .orElse(null);

    }

    /**
     * get item's nextBooking relating to a specified time
     *
     * @param itemId item's id
     * @param now    current time
     * @return BookingItemResponseDto object or null result
     */
    private BookingItemResponseDto getNextBooking(Long itemId, LocalDateTime now) {
        return bookingJpaRepository
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart
                        (itemId, BookingStatus.APPROVED, now, now)
                .map(BookingMapper::toBookingItemResponseDto)
                .orElse(null);
    }

    /**
     * log list of objects in pretty format
     *
     * @param items list of items
     */
    private void logResultList(List<ItemResponseDto> items) {
        String result = items.stream()
                .map(ItemResponseDto::toString)
                .collect(Collectors.joining(", "));
        log.info("Список вещей по запросу: {}", result);

    }

    /**
     * get if exists List of comments by item's id
     *
     * @param itemId item's id
     * @return list of comments or empty list
     */
    private List<CommentResponseDto> getCommentsByItemId(Long itemId) {
        List<Comment> comments = commentJpaRepository.findAllByItemId(itemId);
        return CommentMapper.toCommentResponseDtoList(comments);
    }


}
