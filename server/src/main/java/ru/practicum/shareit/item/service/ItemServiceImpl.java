package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentOutDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.Validation;
import ru.practicum.shareit.util.exceptions.AccessIsNotAllowedException;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.util.exceptions.UnavailableItemException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ItemService implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    /**
     * to add item's data (save and assign identity)
     * throws 404.NOT_FOUND ObjectNotFoundException if user doesn't exist
     * throws 404.NOT_FOUND ObjectNotFoundException if requestId in ItemDto is not null and doesn't exist
     *
     * @param userId  owner's id
     * @param itemDto item to register
     * @return item with assigned id
     */

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUserByIdIfExists(userId);
        ItemRequest itemRequest = getItemRequestIfExists(itemDto);
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item itemWithId = itemRepository.save(item);
        log.info("Зарегистрирована вещь: {}", itemWithId);
        return ItemMapper.toItemDto(itemWithId);
    }

    /**
     * get item by id with lastBooking, nextBooking and comments properties
     * throws 404.NOT_FOUND ObjectNotFoundException if item doesn't exist
     *
     * @param userId user's id
     * @param itemId item's id
     * @return item with lastBooking, nextBooking and comments properties
     */
    @Override
    @Transactional(readOnly = true)
    public ItemOutDto getById(Long userId, Long itemId) {

        Item item = getItemByIdIfExists(itemId);

        LocalDateTime now = LocalDateTime.now();
        BookingItemDto lastBooking = null;
        BookingItemDto nextBooking = null;
        if (isOwner(item, userId)) {
            lastBooking = getLastBooking(itemId, now);
            nextBooking = getNextBooking(itemId, now);
        }

        List<CommentOutDto> commentsDto = getCommentsByItemId(itemId);
        ItemOutDto itemOutDto = ItemMapper.toItemOutDto(item, lastBooking, nextBooking, commentsDto);
        log.info("Вещь с id: {} найдена по запросу пользователя с id {}, {}", itemId, userId, itemOutDto);
        return itemOutDto;
    }


    /**
     * update item object
     * throws 404.NOT_FOUND ObjectNotFoundException if user doesn't exist
     * throws 404.NOT_FOUND ObjectNotFoundException if item doesn't exist
     * throws 404.NOT_FOUND AccessIsNotAllowedException if user trying updating is not item's owner
     *
     * @param userId  owner's id
     * @param itemId  item's id
     * @param itemDto item to update
     * @return updated item
     */
    @Override
    @Transactional
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {

        checkUserExists(userId);
        Item item = getItemByIdIfExists(itemId);
        checkAccessAllowedOnlyForOwner(item, userId);

        Item updatedItem = updateValidFields(item, itemDto);

        itemRepository.save(updatedItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    /**
     * delete item by id
     *
     * @param itemId item's id
     */
    @Override
    @Transactional
    public void deleteById(Long itemId) {
        if (itemRepository.existsById(itemId)) {
            log.info("Удалена вещь с id: {}", itemId);
            itemRepository.deleteById(itemId);
        }
        log.info("Вещи с id: {} не существует", itemId);
    }

    /**
     * get all items of a specific user
     *
     * @param userId user's id
     * @return list of items or empty list
     */
    @Override
    @Transactional(readOnly = true)
    public List<ItemOutDto> getListByUser(Long userId) {

        List<Item> items = itemRepository.findAllByOwnerIdOrderById(userId);
        Map<Item, List<Comment>> mapComments = getCommentsToAllItems(items);
        List<ItemOutDto> itemsResponses = items.stream()
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
     * @return list of items or empty list
     */
    @Override
    @Transactional(readOnly = true)
    public List<ItemOutDto> searchItemsBySubstring(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.searchItemsBySubstring(text);
        Map<Item, List<Comment>> mapComments = getCommentsToAllItems(items);
        List<ItemOutDto> itemsResponses = items.stream()
                .map(item -> getItemResponseDto(item,
                        mapComments.getOrDefault(item, Collections.emptyList()),
                        LocalDateTime.now()))
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
     * @param commentDto comment
     * @param userId     author's id
     * @param itemId     item's id
     * @return comment
     */
    @Override
    @Transactional
    public CommentOutDto addComment(CommentDto commentDto, Long userId, Long itemId) {

        Item item = getItemByIdIfExists(itemId);
        User user = getUserByIdIfExists(userId);
        checkAccessForOwnerNotAllowed(item, userId);
        checkAccessToCommentAllowed(userId, itemId);

        Comment comment = CommentMapper.toComment(commentDto, user, item);
        Comment commentWithId = commentRepository.save(comment);
        log.info("Для вещи c id {} пользователь id {} добавил новый отзыв: {}", itemId, userId, commentWithId);
        return CommentMapper.toCommentOutDto(commentWithId);
    }

    /**
     * update valid fields
     *
     * @param itemDto properties to check and update
     * @param item    item to update
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
     * get User if exists
     * throws 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param userId user's id
     * @return User object
     */
    private User getUserByIdIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(String.format("Пользователя с id %d не существует", userId)));
    }

    /**
     * get Item if exists
     * throws 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param itemId item's id
     * @return Item object
     */
    private Item getItemByIdIfExists(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(String.format("Вещи с id %d не существует", itemId)));
    }

    /**
     * check whether User exists
     * throws 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param userId user's id
     */
    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException(String.format("Пользователя с id %d не существует", userId));
        }
    }


    /**
     * check whether user had item's approved bookings
     * throws 400.BAD_REQUEST UnavailableItemException if hadn't
     *
     * @param itemId item's id
     * @param userId user's id
     */

    private void checkAccessToCommentAllowed(Long userId, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository
                .findAllByItem_IdAndBooker_IdAndStatusAndStartIsBefore(itemId, userId, BookingStatus.APPROVED, now);
        if (bookings.isEmpty()) {
            throw new UnavailableItemException("Вы не вправе оставлять отзывы, "
                    + "поскольку не пользовались данной вещью");
        }
    }


    /**
     * check whether user is not item's owner
     * throws 404.NOT_FOUND AccessIsNotAllowedException if is
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
     * check whether user is item's owner
     * throws 404.NOT_FOUND AccessIsNotAllowedException if isn't
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
     * construct and get ItemOutDto by current time
     *
     * @param item Item
     * @param now  current time
     * @return ItemOutDto object
     */
    private ItemOutDto getItemResponseDto(Item item, List<Comment> comments, LocalDateTime now) {

        Long itemId = item.getId();
        BookingItemDto lastBooking = getLastBooking(itemId, now);
        BookingItemDto nextBooking = getNextBooking(itemId, now);
        List<CommentOutDto> commentsDto = CommentMapper.toCommentOutDtoList(comments);

        return ItemMapper.toItemOutDto(item, lastBooking, nextBooking, commentsDto);
    }

    /**
     * get map ItemId:List of comments - map of comments grouping by items
     *
     * @param items list of items
     * @return map of comments grouping by items
     */
    private Map<Item, List<Comment>> getCommentsToAllItems(List<Item> items) {

        List<Comment> comments = commentRepository.findAllByItemIn(items);
        return comments.stream()
                .collect(Collectors.groupingBy(Comment::getItem));
    }


    /**
     * get item's lastBooking relating to a specified time
     *
     * @param itemId item's id
     * @param now    current time
     * @return BookingItemDto object or null result
     */
    private BookingItemDto getLastBooking(Long itemId, LocalDateTime now) {
        return bookingRepository
                .findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(itemId,
                        BookingStatus.APPROVED, now, now)
                .map(BookingMapper::toBookingItemDto)
                .orElse(null);
    }

    /**
     * get item's nextBooking relating to a specified time
     *
     * @param itemId item's id
     * @param now    current time
     * @return BookingItemDto object or null result
     */
    private BookingItemDto getNextBooking(Long itemId, LocalDateTime now) {
        return bookingRepository
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(itemId,
                        BookingStatus.APPROVED, now, now)
                .map(BookingMapper::toBookingItemDto)
                .orElse(null);
    }

    /**
     * log list of items in pretty format
     *
     * @param items list of items
     */
    private void logResultList(List<ItemOutDto> items) {
        String result = items.stream()
                .map(ItemOutDto::toString)
                .collect(Collectors.joining(", "));
        log.info("Список вещей по запросу: {}", result);

    }

    /**
     * get if exists List of comments by item's id
     *
     * @param itemId item's id
     * @return list of comments or empty list
     */
    private List<CommentOutDto> getCommentsByItemId(Long itemId) {
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        return CommentMapper.toCommentOutDtoList(comments);
    }

    /**
     * get if exists ItemRequest
     * throws 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param itemDto itemDto object
     * @return ItemRequest or null
     */
    private ItemRequest getItemRequestIfExists(ItemDto itemDto) {
        if (itemDto.getRequestId() == null) {
            return null;
        }
        Long requestId = itemDto.getRequestId();
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(String.format("Запроса с id %d не существует", requestId)));
    }


}
