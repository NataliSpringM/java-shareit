package ru.practicum.shareit;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.exceptions.AccessIsNotAllowedException;
import ru.practicum.shareit.util.exceptions.ConflictEmailException;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.util.exceptions.UnavailableItemException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
@Validated
class ShareItTests {
    @Autowired
    private UserController userController;
    @Autowired
    private ItemController itemController;
    @Autowired
    private BookingController bookingController;

    UserDto userAlex1;
    UserDto userEgor2;
    UserDto userAlex3;
    UserDto userOlga4;
    UserDto userAnna5;
    UserDto userWithInvalidEmail;
    UserDto userWithNullName;
    UserDto userWithEmptyName;
    UserDto userWithNullEmail;
    ItemDto screwDriver;
    ItemDto lawnMower;
    ItemDto bike;
    ItemDto noName;
    ItemDto adultBike;
    ItemDto nullDescription;
    ItemDto nullAvailable;
    ItemDto onlyAvailable;
    ItemDto onlyDescription;
    ItemDto onlyName;

    BookingDto bookingItem1Future;
    BookingDto bookingItem1Future2;
    BookingDto bookingItem1Future3;
    BookingDto bookingInvalidStartInPast;
    BookingDto bookingInvalidStartEqualsEnd;
    BookingDto bookingInvalidEndInPast;
    BookingDto bookingInvalidEndBeforeStart;
    BookingDto bookingItem2;
    CommentDto commentToItem1First;
    CommentDto commentToItem2;
    CommentDto commentWithEmptyText;
    CommentDto commentWithoutText;
    Long nonExistingId;

    ShareItTests() {
    }


    @BeforeEach
    public void create() {

        userAlex1 = UserDto.builder().email("Alex@yandex.ru").name("Alexandr Ivanov").build();
        userEgor2 = UserDto.builder().email(" ").name("Egor Egorov").build();
        userAlex3 = UserDto.builder().email("Alex@yandex.ru").name("Alexey Petrov").build();
        userOlga4 = UserDto.builder().email("Olga@yandex.ru").name("Olga Smith").build();
        userAnna5 = UserDto.builder().email("Anna@yandex.ru").name("Anna Smith").build();
        userWithEmptyName = UserDto.builder().name("").email("a@yandex.ru").build();
        userWithInvalidEmail = UserDto.builder().name("Anna").email("email").build();
        userWithNullName = UserDto.builder().email("a@yandex.ru").build();
        userWithNullEmail = UserDto.builder().name("Anna").build();
        screwDriver = ItemDto.builder().name("screwdriver").description("new").available(true).build();
        lawnMower = ItemDto.builder().name("lawn-mower").description("portable").available(false).build();
        bike = ItemDto.builder().name("bike").description("for children").available(true).build();
        adultBike = ItemDto.builder().name("bike").description("adult").available(true).build();
        noName = ItemDto.builder().name("").description("for children").available(true).build();
        nullDescription = ItemDto.builder().name("bike").available(true).build();
        nullAvailable = ItemDto.builder().name("bike").description("adult").build();
        onlyAvailable = ItemDto.builder().available(false).build();
        onlyDescription = ItemDto.builder().description("patched description").build();
        onlyName = ItemDto.builder().name("updated").build();
        bookingItem1Future = BookingDto.builder().itemId(1L)
                .start(LocalDateTime.of(2030, 1, 2, 20, 11, 11))
                .end(LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .build();
        bookingItem1Future2 = BookingDto.builder().itemId(1L)
                .start(LocalDateTime.of(2030, 2, 2, 20, 11, 11))
                .end(LocalDateTime.of(2030, 3, 1, 1, 1, 1))
                .build();
        bookingItem1Future3 = BookingDto.builder().itemId(1L)
                .start(LocalDateTime.of(2030, 4, 2, 20, 11, 11))
                .end(LocalDateTime.of(2030, 4, 3, 1, 1, 1))
                .build();
        bookingItem2 = BookingDto.builder().itemId(2L)
                .start(LocalDateTime.of(2030, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2030, 1, 2, 1, 1, 1))
                .build();
        bookingInvalidStartEqualsEnd = BookingDto.builder().itemId(1L)
                .start(LocalDateTime.of(2030, 4, 2, 20, 11, 11))
                .end(LocalDateTime.of(2030, 4, 2, 20, 11, 11))
                .build();
        bookingInvalidEndBeforeStart = BookingDto.builder().itemId(1L)
                .start(LocalDateTime.of(2030, 12, 1, 1, 1, 1))
                .end(LocalDateTime.of(2030, 1, 1, 1, 1, 1))
                .build();
        bookingInvalidStartInPast = BookingDto.builder().itemId(1L)
                .start(LocalDateTime.now().minusYears(1))
                .end(LocalDateTime.now())
                .build();
        bookingInvalidEndInPast = BookingDto.builder().itemId(1L)
                .start(LocalDateTime.now().minusYears(3))
                .start(LocalDateTime.now().minusYears(1))
                .build();
        commentToItem1First = CommentDto
                .builder().itemId(1L).authorName("Alexey Petrov").text("I like it").build();
        commentToItem2 = CommentDto
                .builder().itemId(2L).authorName("Alexey Petrov").text("Don't use it").build();
        commentWithoutText = CommentDto
                .builder().itemId(2L).authorName("Alexey Petrov").build();
        commentWithEmptyText = CommentDto
                .builder().itemId(2L).authorName("Alexey Petrov").text(" ").build();
        nonExistingId = -1L;

    }

    /**
     * test create and get user with valid data
     */
    @Test
    public void shouldCreateUserAndGetUserById() {

        UserDto user1 = userController.create(userAlex1);
        Optional<UserDto> userOptional = Optional.ofNullable(userController.getById(user1.getId()));
        assertThat(userOptional).hasValueSatisfying(user -> assertThat(user)
                .hasFieldOrPropertyWithValue("id", user.getId())
                .hasFieldOrPropertyWithValue("email", "Alex@yandex.ru")
                .hasFieldOrPropertyWithValue("name", "Alexandr Ivanov"));
    }

    /**
     * test fail get user by invalid id with ObjectNotFoundException
     */
    @Test
    public void shouldFailGetUserByInvalidId() {

        final Long userId = -1L;
        assertThrows(ObjectNotFoundException.class,
                () -> userController.getById(userId),
                "Не выброшено исключение ObjectNotFoundException.");
    }


    /**
     * test fail create user with email registered by another user without ConflictEmailException
     */
    @Test
    public void shouldFailCreateUserWithSameEmail() {

        userController.create(userAlex1);
        assertThrows(DataIntegrityViolationException.class,
                () -> userController.create(userAlex3),
                "Не выброшено исключение DataIntegrityViolationException.");

    }

    /**
     * test fail get user with non-existing id with ObjectNotFoundException
     */
    @Test
    public void shouldFailGetUserWithNonExistingId() {

        assertThrows(ObjectNotFoundException.class,
                () -> userController.getById(nonExistingId),
                "Не выброшено исключение ObjectNotFoundException.");
    }


    /**
     * test update user
     */
    @Test
    public void shouldUpdateUser() {

        UserDto user1 = userController.create(userAlex1);
        UserDto userAlex1Updated = user1.toBuilder().email("AlexSmith@google.ru")
                .name("Alex Smith").build();
        final Long userId = user1.getId();

        userController.update(userAlex1Updated, userId);
        Optional<UserDto> userOptional = Optional.ofNullable(userController.getById(userId));
        assertThat(userOptional)
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", user.getId())
                        .hasFieldOrPropertyWithValue("email", "AlexSmith@google.ru")
                        .hasFieldOrPropertyWithValue("name", "Alex Smith"));

    }

    /**
     * test fail update user with ConflictEmailException trying to update registered by other user email
     */
    @Test
    public void shouldFailUpdateUserWithRegisteredByOtherUserEmail() {

        UserDto user1 = userController.create(userAlex1);
        UserDto user2 = userController.create(userAnna5);
        UserDto user2Updated = user1.toBuilder().email("Alex@yandex.ru")
                .name("Alex Smith").build();
        final Long userId = user2.getId();

        assertThrows(ConflictEmailException.class,
                () -> userController.update(user2Updated, userId),
                "Не выброшено исключение ConflictEmailException.");
    }

    /**
     * test fail update user with non-existing id ObjectNotFoundException
     */
    @Test
    public void shouldFailUpdateUserWithNonExistingId() {

        UserDto user = userController.create(userAlex1);
        UserDto userUpdated = user.toBuilder().email("Alex@yandex.ru")
                .name("Alex Smith").build();

        assertThrows(ObjectNotFoundException.class,
                () -> userController.update(userUpdated, nonExistingId),
                "Не выброшено исключение ObjectNotFoundException.");
    }

    /**
     * test delete user
     */

    @Test
    public void shouldDeleteUser() {

        UserDto user1 = userController.create(userAlex1);
        final Long userId = user1.getId();
        userController.delete(userId);

        List<UserDto> list = userController.getList();
        assertThat(list).asList().hasSize(0);
        assertThat(list).asList().isEmpty();

    }

    /**
     * test get all users' list
     */
    @Test
    public void shouldListUsers() {

        UserDto user1 = userController.create(userAlex1);
        UserDto user4 = userController.create(userOlga4);

        List<UserDto> listUsers = userController.getList();

        assertThat(listUsers).asList().hasSize(2);

        assertThat(listUsers).asList().contains(userController.getById(user1.getId()));
        assertThat(listUsers).asList().contains(userController.getById(user4.getId()));

        assertThat(Optional.of(listUsers.get(0))).hasValueSatisfying(
                user -> AssertionsForClassTypes.assertThat(user)
                        .hasFieldOrPropertyWithValue("name", "Alexandr Ivanov"));

        assertThat(Optional.of(listUsers.get(1))).hasValueSatisfying(
                user -> AssertionsForClassTypes.assertThat(user)
                        .hasFieldOrPropertyWithValue("name", "Olga Smith"));

    }

    /**
     * get user's empty list
     */

    @Test
    public void shouldGetEmptyListUsers() {

        List<UserDto> listUsers = userController.getList();

        assertThat(listUsers).asList().hasSize(0);
        assertThat(listUsers).asList().isEmpty();

    }

    /**
     * test create and get item with valid data
     */
    @Test
    public void shouldCreateItemAndGetItByIdWithoutApprovedBookings() {

        UserDto userDto = userController.create(userAlex1);
        UserDto userDto1 = userController.create(userOlga4);
        Long ownerId = userDto.getId();
        Long bookerId = userDto1.getId();
        ItemDto itemDto = itemController.create(ownerId, screwDriver);
        bookingController.create(bookerId, bookingItem1Future);
        Long bookingId = userDto.getId();
        BookingOutDto bookingApproved = bookingController.updateStatus(ownerId, bookingId, true);
        BookingItemDto bookingItem = BookingMapper.toBookingItemDto(bookingApproved);

        Optional<ItemOutDto> itemOptional = Optional.ofNullable(itemController.getById(ownerId, itemDto.getId()));
        assertThat(itemOptional).hasValueSatisfying(i -> assertThat(i)
                .hasFieldOrPropertyWithValue("id", i.getId())
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("name", "screwdriver")
                .hasFieldOrPropertyWithValue("lastBooking", null)
                .hasFieldOrPropertyWithValue("nextBooking", bookingItem));

    }


    /**
     * test fail get item by non-existing id with ObjectNotFoundException
     */
    @Test
    public void shouldFailGetItemByNonExistingId() {
        UserDto user = userController.create(userAlex1);
        Long userId = user.getId();

        assertThrows(ObjectNotFoundException.class,
                () -> itemController.getById(userId, nonExistingId),
                "Не выброшено исключение ObjectNotFoundException.");
    }


    /**
     * test should fail update item with non-existing user's id with ObjectNotFoundException
     */
    @Test
    public void shouldFailUpdateItemWithNonExistingUserId() {
        UserDto userDto = userController.create(userAlex1);
        final Long userId = userDto.getId();
        ItemDto itemDto = itemController.create(userId, screwDriver);
        ItemDto updatedDescriptionItem = itemDto.toBuilder().description("rusty and old").build();

        assertThrows(ObjectNotFoundException.class,
                () -> itemController.update(nonExistingId, updatedDescriptionItem, itemDto.getId()),
                "Не выброшено исключение ObjectNotFoundException.");

    }

    /**
     * test should fail update item with non-existing item's id with ObjectNotFoundException
     */
    @Test
    public void shouldFailUpdateItemWithNonExistingId() {
        UserDto userDto = userController.create(userAlex1);
        final Long userId = userDto.getId();

        assertThrows(ObjectNotFoundException.class,
                () -> itemController.update(userId, screwDriver, nonExistingId),
                "Не выброшено исключение ObjectNotFoundException.");

    }

    /**
     * test should fail update item when not allowed user's id (user is not an owner of the item)
     * with AccessIsNotAllowedException
     */
    @Test
    public void shouldFailUpdateItemByNotOwnerId() {
        UserDto userDto = userController.create(userAlex1);
        final Long ownerId = userDto.getId();
        UserDto userDto1 = userController.create(userAnna5);
        final Long notOwnerId = userDto1.getId();
        ItemDto itemDto = itemController.create(ownerId, screwDriver);
        ItemDto updatedDescriptionItem = itemDto.toBuilder().description("rusty and old").build();

        assertThrows(AccessIsNotAllowedException.class,
                () -> itemController.update(notOwnerId, updatedDescriptionItem, itemDto.getId()),
                "Не выброшено исключение AccessIsNotAllowedException.");

    }

    /**
     * test update item with all properties in Dto object
     */
    @Test
    public void shouldUpdateItem() {
        UserDto userDto = userController.create(userAlex1);
        final Long userId = userDto.getId();
        ItemDto itemDto = itemController.create(userId, screwDriver);
        ItemDto updatedDescriptionItem = itemDto.toBuilder().description("rusty and old").build();
        itemController.update(userId, updatedDescriptionItem, itemDto.getId());

        Optional<ItemOutDto> itemOptional = Optional.ofNullable(itemController.getById(userId, itemDto.getId()));
        assertThat(itemOptional).hasValueSatisfying(item -> assertThat(item)
                .hasFieldOrPropertyWithValue("id", item.getId())
                .hasFieldOrPropertyWithValue("description", "rusty and old")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("name", "screwdriver"));

    }

    /**
     * test update item with only available property in Dto object
     */
    @Test
    public void shouldUpdateItemWithAvailableOnly() { // добавление вещи
        UserDto userDto = userController.create(userAlex1);
        final Long userId = userDto.getId();
        ItemDto itemDto = itemController.create(userId, screwDriver);
        itemController.update(userId, onlyAvailable, itemDto.getId());

        Optional<ItemOutDto> itemOptional = Optional.ofNullable(itemController.getById(userId, itemDto.getId()));
        assertThat(itemOptional).hasValueSatisfying(item -> assertThat(item)
                .hasFieldOrPropertyWithValue("id", item.getId())
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", false)
                .hasFieldOrPropertyWithValue("name", "screwdriver"));

    }

    /**
     * test update item with only description property in Dto object
     */
    @Test
    public void shouldUpdateItemWithDescriptionOnly() {
        UserDto userDto = userController.create(userAlex1);
        final Long userId = userDto.getId();
        ItemDto itemDto = itemController.create(userId, screwDriver);
        itemController.update(userId, onlyDescription, itemDto.getId());

        Optional<ItemOutDto> itemOptional = Optional.ofNullable(itemController.getById(userId, itemDto.getId()));
        assertThat(itemOptional).hasValueSatisfying(item -> assertThat(item)
                .hasFieldOrPropertyWithValue("id", item.getId())
                .hasFieldOrPropertyWithValue("description", "patched description")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("name", "screwdriver"));

    }

    /**
     * test update item with only name property in Dto object
     */
    @Test
    public void shouldUpdateItemWithNameOnly() {
        UserDto userDto = userController.create(userAlex1);
        final Long userId = userDto.getId();
        ItemDto itemDto = itemController.create(userId, screwDriver);
        itemController.update(userId, onlyName, itemDto.getId());

        Optional<ItemOutDto> itemOptional = Optional.ofNullable(itemController.getById(userId, itemDto.getId()));
        assertThat(itemOptional).hasValueSatisfying(item -> assertThat(item)
                .hasFieldOrPropertyWithValue("id", item.getId())
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("name", "updated"));

    }

    /**
     * test delete item
     */
    @Test
    public void shouldDeleteItem() { // удаление вещи

        UserDto userDto = userController.create(userAlex1);
        ItemDto itemDto = itemController.create(1L, screwDriver);
        ItemOutDto itemOutDto = ItemMapper.toItemOutDto(itemDto);
        final Long userId = userDto.getId();
        final Long itemId = itemDto.getId();
        List<ItemOutDto> listWithItem = itemController.getListByUser(userId);
        assertThat(listWithItem).asList().hasSize(1);
        assertThat(listWithItem).asList().contains(itemOutDto);

        itemController.delete(itemId);
        List<ItemOutDto> list = itemController.getListByUser(userId);
        assertThat(list).asList().hasSize(0);
        assertThat(list).asList().isEmpty();

    }

    /**
     * test get list of the items of a specific user
     */
    @Test
    public void shouldListItemsByUser() {

        UserDto user1 = userController.create(userAlex1);
        UserDto user4 = userController.create(userOlga4);
        final Long user1Id = user1.getId();
        final Long user4Id = user4.getId();

        ItemDto item1Dto = itemController.create(user1Id, screwDriver);
        ItemDto item2Dto = itemController.create(user1Id, lawnMower);
        ItemDto item3Dto = itemController.create(user4Id, bike);
        ItemOutDto item1OutDto = ItemMapper.toItemOutDto(item1Dto);
        ItemOutDto item2OutDto = ItemMapper.toItemOutDto(item2Dto);
        ItemOutDto item3OutDto = ItemMapper.toItemOutDto(item3Dto);

        List<ItemOutDto> listItems = itemController.getListByUser(user1Id);
        List<ItemOutDto> list2Items = itemController.getListByUser(user4Id);

        assertThat(listItems).asList().hasSize(2);

        assertThat(listItems).asList().contains(item1OutDto);
        assertThat(listItems).asList().contains(item2OutDto);

        assertThat(Optional.of(listItems.get(0))).hasValueSatisfying(
                user -> AssertionsForClassTypes.assertThat(user)
                        .hasFieldOrPropertyWithValue("available", true));

        assertThat(Optional.of(listItems.get(1))).hasValueSatisfying(
                user -> AssertionsForClassTypes.assertThat(user)
                        .hasFieldOrPropertyWithValue("available", false));

        assertThat(list2Items).asList().hasSize(1);
        assertThat(list2Items).asList().contains(item3OutDto);
        assertThat(Optional.of(listItems.get(0))).hasValueSatisfying(
                user -> AssertionsForClassTypes.assertThat(user)
                        .hasFieldOrPropertyWithValue("available", true));


    }

    /**
     * test search item by substring in name or description, excluding items with unavailable status
     */
    @Test
    public void shouldSearchItemByNameOrDescription() {

        UserDto user1 = userController.create(userAlex1);
        UserDto user4 = userController.create(userOlga4);
        final Long user1Id = user1.getId();
        final Long user4Id = user4.getId();

        ItemDto item1Dto = itemController.create(user1Id, screwDriver);
        ItemDto item2Dto = itemController.create(user1Id, lawnMower);
        ItemDto item3Dto = itemController.create(user4Id, bike);

        // получаем список доступных вещей, содержащих в названии или описании подстроку er без учета регистра
        // проверяем корректность полученных данных - 1 вещь,

        List<ItemOutDto> listItems = itemController.searchItemsBySubstring("Er");

        assertThat(listItems).asList().hasSize(1);

        assertThat(listItems).asList().startsWith(itemController.getById(user1.getId(), item1Dto.getId()));
        assertThat(listItems).asList().doesNotContain(itemController.getById(user1.getId(), item2Dto.getId()));

        assertThat(Optional.of(listItems.get(0))).hasValueSatisfying(
                item -> AssertionsForClassTypes.assertThat(item)
                        .hasFieldOrPropertyWithValue("name", "screwdriver"));

        // получаем список доступных вещей, содержащих в названии или описании подстроку er без учета регистра
        // проверяем корректность полученных данных - 2 вещи,
        List<ItemOutDto> list2Items = itemController.searchItemsBySubstring("e");

        assertThat(list2Items).asList().hasSize(2);

        assertThat(list2Items).asList().contains(itemController.getById(user4.getId(), item3Dto.getId()));

        assertThat(Optional.of(list2Items.get(0)))
                .hasValueSatisfying(item -> AssertionsForClassTypes.assertThat(item)
                        .hasFieldOrPropertyWithValue("name", "screwdriver"));
        assertThat(Optional.of(list2Items.get(1)))
                .hasValueSatisfying(item -> AssertionsForClassTypes.assertThat(item)
                        .hasFieldOrPropertyWithValue("name", "bike"));

    }

    /**
     * test create and get booking by owner with valid data
     */
    @Test
    public void shouldCreateBookingAndGetItByIdByOwner() { // добавление бронирования
        UserDto userDto = userController.create(userAlex1);
        UserDto userDto1 = userController.create(userOlga4);
        Long ownerId = userDto.getId();
        Long bookerId = userDto1.getId();
        User owner = UserMapper.toUser(userDto);
        User booker = UserMapper.toUser(userDto1);
        ItemDto itemDto = itemController.create(ownerId, screwDriver);
        Item item = ItemMapper.toItem(itemDto, owner, null);
        bookingController.create(bookerId, bookingItem1Future);

        Optional<BookingOutDto> bookingOptional =
                Optional.ofNullable(bookingController.getById(ownerId, itemDto.getId()));
        assertThat(bookingOptional).hasValueSatisfying(booking -> assertThat(booking)
                .hasFieldOrPropertyWithValue("id", booking.getId())
                .hasFieldOrPropertyWithValue("start",
                        LocalDateTime.of(2030, 1, 2, 20, 11, 11))
                .hasFieldOrPropertyWithValue("end",
                        LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING));

    }

    /**
     * test create and get booking by booker with valid data
     */
    @Test
    public void shouldCreateBookingAndGetItByIdByBooker() {
        UserDto userDto = userController.create(userAlex1);
        UserDto userDto1 = userController.create(userOlga4);
        Long ownerId = userDto.getId();
        Long bookerId = userDto1.getId();
        User owner = UserMapper.toUser(userDto);
        User booker = UserMapper.toUser(userDto1);
        ItemDto itemDto = itemController.create(ownerId, screwDriver);
        Item item = ItemMapper.toItem(itemDto, owner, null);
        bookingController.create(bookerId, bookingItem1Future);

        Optional<BookingOutDto> bookingOptional =
                Optional.ofNullable(bookingController.getById(bookerId, itemDto.getId()));
        assertThat(bookingOptional).hasValueSatisfying(booking -> assertThat(booking)
                .hasFieldOrPropertyWithValue("id", booking.getId())
                .hasFieldOrPropertyWithValue("start",
                        LocalDateTime.of(2030, 1, 2, 20, 11, 11))
                .hasFieldOrPropertyWithValue("end",
                        LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING));

    }


    /**
     * test fail create booking with unavailable item status with UnavailableItemException
     */
    @Test
    public void shouldFailCreateBookingWithUnavailableItemStatus() {
        userController.create(userAlex1);
        UserDto userDto1 = userController.create(userOlga4);
        Long ownerId = userDto1.getId();
        Long bookerId = userDto1.getId();
        itemController.create(ownerId, screwDriver);
        itemController.create(ownerId, lawnMower);


        assertThrows(UnavailableItemException.class,
                () -> bookingController.create(bookerId, bookingItem2),
                "Не выброшено исключение UnavailableItemException");

    }

    /**
     * test fail create booking with unavailable item status with AccessIsNotAllowedException
     */
    @Test
    public void shouldFailCreateBookingIfUserIsOwner() {
        UserDto userDto = userController.create(userAlex1);
        Long ownerId = userDto.getId();
        itemController.create(ownerId, screwDriver);

        assertThrows(AccessIsNotAllowedException.class,
                () -> bookingController.create(ownerId, bookingItem1Future3),
                "Не выброшено исключение AccessIsNotAllowedException");

    }


    /**
     * test fail create booking with non-existing item with ObjectNotFoundException
     */
    @Test
    public void shouldFailCreateBookingWithNonExistingItem() {

        UserDto userDto1 = userController.create(userOlga4);
        Long bookerId = userDto1.getId();
        assertThrows(ObjectNotFoundException.class,
                () -> bookingController.create(bookerId, bookingItem1Future),
                "Не выброшено исключение ObjectNotFoundException");

    }

    /**
     * test fail create booking from non-existing user with ObjectNotFoundException
     */
    @Test
    public void shouldFailCreateBookingWithNonExistingUser() {

        assertThrows(ObjectNotFoundException.class,
                () -> bookingController.create(nonExistingId, bookingItem1Future3),
                "Не выброшено исключение ObjectNotFoundException");

    }


    /**
     * test create and fail get booking by user without access with AccessIsNotAllowedException
     */
    @Test
    public void shouldFailCreateBookingAndGetItByIdByUserWithoutAccess() { // добавление бронирования
        UserDto userDto1 = userController.create(userAlex1);
        UserDto userDto2 = userController.create(userOlga4);
        UserDto userDto3 = userController.create(userAnna5);
        Long ownerId = userDto1.getId();
        Long bookerId = userDto2.getId();
        Long userWithoutAccessId = userDto3.getId();
        ItemDto itemDto = itemController.create(ownerId, screwDriver);
        bookingController.create(bookerId, bookingItem1Future);

        assertThrows(AccessIsNotAllowedException.class,
                () -> bookingController.getById(userWithoutAccessId, itemDto.getId()),
                "Не выброшено исключение AccessIsNotAllowedException.");

    }

    /**
     * test update status of approving for booking - to set APPROVE bookingStatus
     */
    @Test
    public void shouldSetApprovedStatusOfBooking() {
        UserDto userDto = userController.create(userAlex1);
        UserDto userDto1 = userController.create(userOlga4);
        Long ownerId = userDto.getId();
        Long bookerId = userDto1.getId();
        User owner = UserMapper.toUser(userDto);
        User booker = UserMapper.toUser(userDto1);
        ItemDto itemDto = itemController.create(ownerId, screwDriver);
        Item item = ItemMapper.toItem(itemDto, owner, null);
        BookingOutDto bookingFirst = bookingController.create(bookerId, bookingItem1Future);
        Long bookingId = bookingFirst.getId();
        bookingController.updateStatus(ownerId, bookingId, true);


        Optional<BookingOutDto> bookingOptional =
                Optional.ofNullable(bookingController.getById(ownerId, itemDto.getId()));
        assertThat(bookingOptional).hasValueSatisfying(booking -> assertThat(booking)
                .hasFieldOrPropertyWithValue("id", booking.getId())
                .hasFieldOrPropertyWithValue("start",
                        LocalDateTime.of(2030, 1, 2, 20, 11, 11))
                .hasFieldOrPropertyWithValue("end",
                        LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED));

    }

    /**
     * test update status of approving for booking - to set REJECT bookingStatus
     */
    @Test
    public void shouldSetRejectedStatusOfBooking() {
        UserDto userDto = userController.create(userAlex1);
        UserDto userDto1 = userController.create(userOlga4);
        Long ownerId = userDto.getId();
        Long bookerId = userDto1.getId();
        User owner = UserMapper.toUser(userDto);
        User booker = UserMapper.toUser(userDto1);
        ItemDto itemDto = itemController.create(ownerId, screwDriver);
        Item item = ItemMapper.toItem(itemDto, owner, null);
        BookingOutDto bookingFirst = bookingController.create(bookerId, bookingItem1Future);
        Long bookingId = bookingFirst.getId();
        bookingController.updateStatus(ownerId, bookingId, false);


        Optional<BookingOutDto> bookingOptional =
                Optional.ofNullable(bookingController.getById(ownerId, itemDto.getId()));
        assertThat(bookingOptional).hasValueSatisfying(booking -> assertThat(booking)
                .hasFieldOrPropertyWithValue("id", booking.getId())
                .hasFieldOrPropertyWithValue("start",
                        LocalDateTime.of(2030, 1, 2, 20, 11, 11))
                .hasFieldOrPropertyWithValue("end",
                        LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("status", BookingStatus.REJECTED));

    }

    /**
     * test fail update status of approving for booking - to update NOT WAITING - REJECT bookingStatus
     * with UnavailableItemException
     */
    @Test
    public void shouldFailChangeRejectedStatusOfBooking() {
        UserDto userDto = userController.create(userAlex1);
        UserDto userDto1 = userController.create(userOlga4);
        Long ownerId = userDto.getId();
        Long bookerId = userDto1.getId();
        User owner = UserMapper.toUser(userDto);
        User booker = UserMapper.toUser(userDto1);
        ItemDto itemDto = itemController.create(ownerId, screwDriver);
        Item item = ItemMapper.toItem(itemDto, owner, null);
        BookingOutDto bookingFirst = bookingController.create(bookerId, bookingItem1Future);
        Long bookingId = bookingFirst.getId();
        bookingController.updateStatus(ownerId, bookingId, false);


        Optional<BookingOutDto> bookingOptional =
                Optional.ofNullable(bookingController.getById(ownerId, itemDto.getId()));
        assertThat(bookingOptional).hasValueSatisfying(booking -> assertThat(booking)
                .hasFieldOrPropertyWithValue("id", booking.getId())
                .hasFieldOrPropertyWithValue("start",
                        LocalDateTime.of(2030, 1, 2, 20, 11, 11))
                .hasFieldOrPropertyWithValue("end",
                        LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("status", BookingStatus.REJECTED));

        assertThrows(UnavailableItemException.class,
                () -> bookingController.updateStatus(ownerId, bookingId, true),
                "Не выброшено исключение UnavailableItemException.");


    }

    /**
     * test fail update status of approving for booking - to update NOT WAITING - APPROVED bookingStatus
     * with UnavailableItemException
     */
    @Test
    public void shouldFailChangeApprovedStatusOfBooking() {
        UserDto userDto = userController.create(userAlex1);
        UserDto userDto1 = userController.create(userOlga4);
        Long ownerId = userDto.getId();
        Long bookerId = userDto1.getId();
        User owner = UserMapper.toUser(userDto);
        User booker = UserMapper.toUser(userDto1);
        ItemDto itemDto = itemController.create(ownerId, screwDriver);
        Item item = ItemMapper.toItem(itemDto, owner, null);
        BookingOutDto bookingFirst = bookingController.create(bookerId, bookingItem1Future);
        Long bookingId = bookingFirst.getId();
        bookingController.updateStatus(ownerId, bookingId, true);


        Optional<BookingOutDto> bookingOptional =
                Optional.ofNullable(bookingController.getById(ownerId, itemDto.getId()));
        assertThat(bookingOptional).hasValueSatisfying(booking -> assertThat(booking)
                .hasFieldOrPropertyWithValue("id", booking.getId())
                .hasFieldOrPropertyWithValue("start",
                        LocalDateTime.of(2030, 1, 2, 20, 11, 11))
                .hasFieldOrPropertyWithValue("end",
                        LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED));

        assertThrows(UnavailableItemException.class,
                () -> bookingController.updateStatus(ownerId, bookingId, false),
                "Не выброшено исключение UnavailableItemException.");


    }

    /**
     * test fail get list of owner's bookings by non-existing user with ObjectNotFoundException
     */
    @Test
    public void shouldFailGetListOfAllBookingByNonExistingUserAsOwner() {

        assertThrows(ObjectNotFoundException.class,
                () -> bookingController.getListByOwner(nonExistingId, BookingState.ALL, 0, 10),
                "Не выброшено исключение ObjectNotFoundException.");

    }

    /**
     * test get list of owner's bookings with default state ALL
     */
    @Test
    public void shouldGetListOfAllBookingByOwner() {
        UserDto userDto1 = userController.create(userAlex1);
        UserDto userDto2 = userController.create(userOlga4);
        Long ownerId = userDto1.getId();
        Long bookerId = userDto2.getId();
        User owner = UserMapper.toUser(userDto1);
        User booker = UserMapper.toUser(userDto2);
        ItemDto itemDto1 = itemController.create(ownerId, screwDriver);
        Item item1 = ItemMapper.toItem(itemDto1, owner, null);
        itemController.create(ownerId, adultBike);

        bookingController.create(bookerId, bookingItem1Future);
        BookingOutDto bookingSecondFutureItem1 = bookingController.create(bookerId, bookingItem1Future2);
        BookingOutDto bookingThirdFutureItem1 = bookingController.create(bookerId, bookingItem1Future3);
        BookingOutDto bookingFirstItem2 = bookingController.create(bookerId, bookingItem2);

        Long booking2FutureItem1Id = bookingSecondFutureItem1.getId();
        Long booking3FutureItem1Id = bookingThirdFutureItem1.getId();
        Long booking4Item2Id = bookingFirstItem2.getId();

        bookingController.updateStatus(ownerId, booking2FutureItem1Id, false);
        BookingOutDto booking3ApprovedItem1Future = bookingController.updateStatus(ownerId,
                booking3FutureItem1Id, true);
        BookingOutDto booking4ApprovedItem2 = bookingController.updateStatus(ownerId,
                booking4Item2Id, true);

        List<BookingOutDto> listAllBookings = bookingController.getListByOwner(ownerId, BookingState.ALL,
                0, 10);

        assertThat(listAllBookings).asList().hasSize(4);
        assertThat(listAllBookings).asList().startsWith(booking3ApprovedItem1Future);
        assertThat(listAllBookings).asList().endsWith(booking4ApprovedItem2);

        assertThat(Optional.of(listAllBookings.get(0))).hasValueSatisfying(
                booking -> AssertionsForClassTypes.assertThat(booking)
                        .hasFieldOrPropertyWithValue("id", booking.getId())
                        .hasFieldOrPropertyWithValue("start",
                                LocalDateTime.of(2030, 4, 2, 20, 11, 11))
                        .hasFieldOrPropertyWithValue("end",
                                LocalDateTime.of(2030, 4, 3, 1, 1, 1))
                        .hasFieldOrPropertyWithValue("booker", booker)
                        .hasFieldOrPropertyWithValue("item", item1)
                        .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED));

    }

    /**
     * test get list of owner's bookings with state FUTURE
     */
    @Test
    public void shouldGetListOfFutureBookingsByOwner() {
        UserDto userDto1 = userController.create(userAlex1);
        UserDto userDto2 = userController.create(userOlga4);
        Long ownerId = userDto1.getId();
        Long bookerId = userDto2.getId();
        User owner = UserMapper.toUser(userDto1);
        User booker = UserMapper.toUser(userDto2);
        ItemDto itemDto1 = itemController.create(ownerId, screwDriver);
        Item item1 = ItemMapper.toItem(itemDto1, owner, null);
        itemController.create(ownerId, adultBike);

        bookingController.create(bookerId, bookingItem1Future);
        BookingOutDto bookingSecondFutureItem1 = bookingController.create(bookerId, bookingItem1Future2);
        BookingOutDto bookingThirdFutureItem1 = bookingController.create(bookerId, bookingItem1Future3);
        BookingOutDto bookingCurrentItem2 = bookingController.create(bookerId, bookingItem2);

        Long booking2FutureItem1Id = bookingSecondFutureItem1.getId();
        Long booking3FutureItem1Id = bookingThirdFutureItem1.getId();
        Long booking4Item2Id = bookingCurrentItem2.getId();

        bookingController.updateStatus(ownerId, booking2FutureItem1Id, false);
        BookingOutDto booking3ApprovedItem1Future = bookingController.updateStatus(ownerId,
                booking3FutureItem1Id, true);
        BookingOutDto booking4ApprovedItem2 = bookingController.updateStatus(ownerId,
                booking4Item2Id, true);

        List<BookingOutDto> listFutureBookings = bookingController.getListByOwner(ownerId,
                BookingState.FUTURE, 0, 10);

        assertThat(listFutureBookings).asList().hasSize(4);
        assertThat(listFutureBookings).asList().startsWith(booking3ApprovedItem1Future);
        assertThat(listFutureBookings).asList().endsWith(booking4ApprovedItem2);

        assertThat(Optional.of(listFutureBookings.get(0))).hasValueSatisfying(
                booking -> AssertionsForClassTypes.assertThat(booking)
                        .hasFieldOrPropertyWithValue("id", booking.getId())
                        .hasFieldOrPropertyWithValue("start",
                                LocalDateTime.of(2030, 4, 2, 20, 11, 11))
                        .hasFieldOrPropertyWithValue("end",
                                LocalDateTime.of(2030, 4, 3, 1, 1, 1))
                        .hasFieldOrPropertyWithValue("booker", booker)
                        .hasFieldOrPropertyWithValue("item", item1)
                        .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED));

    }

    /**
     * test get list of owner's bookings with state WAITING
     */
    @Test
    public void shouldGetListOfWaitingBookingsByOwner() {
        UserDto userDto1 = userController.create(userAlex1);
        UserDto userDto2 = userController.create(userOlga4);
        Long ownerId = userDto1.getId();
        Long bookerId = userDto2.getId();
        User owner = UserMapper.toUser(userDto1);
        User booker = UserMapper.toUser(userDto2);
        ItemDto itemDto1 = itemController.create(ownerId, screwDriver);
        Item item1 = ItemMapper.toItem(itemDto1, owner, null);
        itemController.create(ownerId, adultBike);

        BookingOutDto bookingFirstFutureItem1 = bookingController.create(bookerId, bookingItem1Future);
        BookingOutDto bookingSecondFutureItem1 = bookingController.create(bookerId, bookingItem1Future2);
        BookingOutDto bookingThirdFutureItem1 = bookingController.create(bookerId, bookingItem1Future3);
        BookingOutDto bookingCurrentItem2 = bookingController.create(bookerId, bookingItem2);

        Long booking2FutureItem1Id = bookingSecondFutureItem1.getId();
        Long booking3FutureItem1Id = bookingThirdFutureItem1.getId();
        Long booking4Item2Id = bookingCurrentItem2.getId();

        bookingController.updateStatus(ownerId, booking2FutureItem1Id, false);
        bookingController.updateStatus(ownerId, booking3FutureItem1Id, true);
        bookingController.updateStatus(ownerId, booking4Item2Id, true);

        List<BookingOutDto> listWaitingBookings = bookingController.getListByOwner(ownerId,
                BookingState.WAITING, 0, 10);

        assertThat(listWaitingBookings).asList().hasSize(1);
        assertThat(listWaitingBookings).asList().contains(bookingFirstFutureItem1);

        assertThat(Optional.of(listWaitingBookings.get(0))).hasValueSatisfying(
                booking -> AssertionsForClassTypes.assertThat(booking)
                        .hasFieldOrPropertyWithValue("id", booking.getId())
                        .hasFieldOrPropertyWithValue("start",
                                LocalDateTime.of(2030, 1, 2, 20, 11, 11))
                        .hasFieldOrPropertyWithValue("end",
                                LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                        .hasFieldOrPropertyWithValue("booker", booker)
                        .hasFieldOrPropertyWithValue("item", item1)
                        .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING));


    }

    /**
     * test get list of owner's bookings with state REJECTED
     */
    @Test
    public void shouldGetListOfRejectedBookingsByOwner() {
        UserDto userDto1 = userController.create(userAlex1);
        UserDto userDto2 = userController.create(userOlga4);
        Long ownerId = userDto1.getId();
        Long bookerId = userDto2.getId();
        User owner = UserMapper.toUser(userDto1);
        User booker = UserMapper.toUser(userDto2);
        ItemDto itemDto1 = itemController.create(ownerId, screwDriver);
        Item item1 = ItemMapper.toItem(itemDto1, owner, null);
        itemController.create(ownerId, adultBike);

        bookingController.create(bookerId, bookingItem1Future);
        BookingOutDto bookingSecondFutureItem1 = bookingController.create(bookerId, bookingItem1Future2);
        BookingOutDto bookingThirdFutureItem1 = bookingController.create(bookerId, bookingItem1Future3);
        BookingOutDto bookingCurrentItem2 = bookingController.create(bookerId, bookingItem2);

        Long booking2FutureItem1Id = bookingSecondFutureItem1.getId();
        Long booking3FutureItem1Id = bookingThirdFutureItem1.getId();
        Long booking4Item2Id = bookingCurrentItem2.getId();

        BookingOutDto booking2RejectedItem1Future = bookingController.updateStatus(ownerId,
                booking2FutureItem1Id, false);
        bookingController.updateStatus(ownerId, booking3FutureItem1Id, true);
        bookingController.updateStatus(ownerId, booking4Item2Id, true);

        List<BookingOutDto> listRejectedBookings = bookingController.getListByOwner(ownerId,
                BookingState.REJECTED, 0, 10);

        assertThat(listRejectedBookings).asList().hasSize(1);
        assertThat(listRejectedBookings).asList().contains(booking2RejectedItem1Future);

        assertThat(Optional.of(listRejectedBookings.get(0))).hasValueSatisfying(
                booking -> AssertionsForClassTypes.assertThat(booking)
                        .hasFieldOrPropertyWithValue("id", booking.getId())
                        .hasFieldOrPropertyWithValue("start",
                                LocalDateTime.of(2030, 2, 2, 20, 11, 11))
                        .hasFieldOrPropertyWithValue("end",
                                LocalDateTime.of(2030, 3, 1, 1, 1, 1))
                        .hasFieldOrPropertyWithValue("booker", booker)
                        .hasFieldOrPropertyWithValue("item", item1)
                        .hasFieldOrPropertyWithValue("status", BookingStatus.REJECTED));


    }

    /**
     * get user's empty list by owner
     */
    @Test
    public void shouldGetEmptyBookingListByOwner() {
        UserDto userDto = userController.create(userAlex1);
        userController.create(userOlga4);
        Long ownerId = userDto.getId();

        List<BookingOutDto> listUsers = bookingController.getListByOwner(ownerId, BookingState.ALL,
                0, 10);

        assertThat(listUsers).asList().hasSize(0);
        assertThat(listUsers).asList().isEmpty();

    }

    /**
     * test fail get list of user's bookings by non-existing user with ObjectNotFoundException
     */
    @Test
    public void shouldFailGetListOfAllBookingByNonExistingUserAsBooker() {

        assertThrows(ObjectNotFoundException.class,
                () -> bookingController.getListByBooker(nonExistingId, BookingState.ALL, 0, 10),
                "Не выброшено исключение ObjectNotFoundException.");

    }

    /**
     * test get list of user's bookings with default state ALL
     */
    @Test
    public void shouldGetListOfAllBookingByBooker() {
        UserDto userDto1 = userController.create(userAlex1);
        UserDto userDto2 = userController.create(userOlga4);
        Long ownerId = userDto1.getId();
        Long bookerId = userDto2.getId();
        User owner = UserMapper.toUser(userDto1);
        User booker = UserMapper.toUser(userDto2);
        ItemDto itemDto1 = itemController.create(ownerId, screwDriver);
        Item item1 = ItemMapper.toItem(itemDto1, owner, null);
        bookingController.create(bookerId, bookingItem1Future);
        itemController.create(ownerId, adultBike);

        BookingOutDto bookingSecondFutureItem1 = bookingController.create(bookerId, bookingItem1Future2);
        BookingOutDto bookingThirdFutureItem1 = bookingController.create(bookerId, bookingItem1Future3);
        BookingOutDto bookingCurrentItem2 = bookingController.create(bookerId, bookingItem2);

        Long booking2FutureItem1Id = bookingSecondFutureItem1.getId();
        Long booking3FutureItem1Id = bookingThirdFutureItem1.getId();
        Long booking4Item2Id = bookingCurrentItem2.getId();

        bookingController.updateStatus(ownerId, booking2FutureItem1Id, false);
        BookingOutDto booking3ApprovedItem1Future = bookingController.updateStatus(ownerId,
                booking3FutureItem1Id, true);
        BookingOutDto booking4ApprovedItem2 = bookingController.updateStatus(ownerId,
                booking4Item2Id, true);

        List<BookingOutDto> listAllBookings = bookingController.getListByOwner(ownerId, BookingState.ALL,
                0, 10);

        assertThat(listAllBookings).asList().hasSize(4);
        assertThat(listAllBookings).asList().startsWith(booking3ApprovedItem1Future);
        assertThat(listAllBookings).asList().endsWith(booking4ApprovedItem2);

        assertThat(Optional.of(listAllBookings.get(0))).hasValueSatisfying(
                booking -> AssertionsForClassTypes.assertThat(booking)
                        .hasFieldOrPropertyWithValue("id", booking.getId())
                        .hasFieldOrPropertyWithValue("start",
                                LocalDateTime.of(2030, 4, 2, 20, 11, 11))
                        .hasFieldOrPropertyWithValue("end",
                                LocalDateTime.of(2030, 4, 3, 1, 1, 1))
                        .hasFieldOrPropertyWithValue("booker", booker)
                        .hasFieldOrPropertyWithValue("item", item1)
                        .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED));

    }

    /**
     * test get list of user's bookings with state FUTURE
     */
    @Test
    public void shouldGetListOfFutureBookingsByBooker() {
        UserDto userDto1 = userController.create(userAlex1);
        UserDto userDto2 = userController.create(userOlga4);
        Long ownerId = userDto1.getId();
        Long bookerId = userDto2.getId();
        User owner = UserMapper.toUser(userDto1);
        User booker = UserMapper.toUser(userDto2);
        ItemDto itemDto1 = itemController.create(ownerId, screwDriver);
        Item item1 = ItemMapper.toItem(itemDto1, owner, null);
        itemController.create(ownerId, adultBike);

        bookingController.create(bookerId, bookingItem1Future);
        BookingOutDto bookingSecondFutureItem1 = bookingController.create(bookerId, bookingItem1Future2);
        BookingOutDto bookingThirdFutureItem1 = bookingController.create(bookerId, bookingItem1Future3);
        BookingOutDto bookingCurrentItem2 = bookingController.create(bookerId, bookingItem2);

        Long booking2FutureItem1Id = bookingSecondFutureItem1.getId();
        Long booking3FutureItem1Id = bookingThirdFutureItem1.getId();
        Long booking4Item2Id = bookingCurrentItem2.getId();

        bookingController.updateStatus(ownerId, booking2FutureItem1Id, false);
        BookingOutDto booking3ApprovedItem1Future = bookingController.updateStatus(ownerId,
                booking3FutureItem1Id, true);
        BookingOutDto booking4ApprovedItem2 = bookingController.updateStatus(ownerId,
                booking4Item2Id, true);

        List<BookingOutDto> listFutureBookings = bookingController.getListByBooker(bookerId, BookingState.FUTURE, 0, 10);

        assertThat(listFutureBookings).asList().hasSize(4);
        assertThat(listFutureBookings).asList().startsWith(booking3ApprovedItem1Future);
        assertThat(listFutureBookings).asList().endsWith(booking4ApprovedItem2);

        assertThat(Optional.of(listFutureBookings.get(0))).hasValueSatisfying(
                booking -> AssertionsForClassTypes.assertThat(booking)
                        .hasFieldOrPropertyWithValue("id", booking.getId())
                        .hasFieldOrPropertyWithValue("start",
                                LocalDateTime.of(2030, 4, 2, 20, 11, 11))
                        .hasFieldOrPropertyWithValue("end",
                                LocalDateTime.of(2030, 4, 3, 1, 1, 1))
                        .hasFieldOrPropertyWithValue("booker", booker)
                        .hasFieldOrPropertyWithValue("item", item1)
                        .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED));


    }

    /**
     * test get list of user's bookings with state WAITING
     */
    @Test
    public void shouldGetListOfWaitingBookingsByBooker() {
        UserDto userDto1 = userController.create(userAlex1);
        UserDto userDto2 = userController.create(userOlga4);
        Long ownerId = userDto1.getId();
        Long bookerId = userDto2.getId();
        User owner = UserMapper.toUser(userDto1);
        User booker = UserMapper.toUser(userDto2);
        ItemDto itemDto1 = itemController.create(ownerId, screwDriver);
        Item item1 = ItemMapper.toItem(itemDto1, owner, null);
        itemController.create(ownerId, adultBike);
        BookingOutDto bookingFirstFutureItem1 = bookingController.create(bookerId, bookingItem1Future);
        BookingOutDto bookingSecondFutureItem1 = bookingController.create(bookerId, bookingItem1Future2);
        BookingOutDto bookingThirdFutureItem1 = bookingController.create(bookerId, bookingItem1Future3);
        BookingOutDto bookingCurrentItem2 = bookingController.create(bookerId, bookingItem2);

        Long booking2FutureItem1Id = bookingSecondFutureItem1.getId();
        Long booking3FutureItem1Id = bookingThirdFutureItem1.getId();
        Long booking4Item2Id = bookingCurrentItem2.getId();

        bookingController.updateStatus(ownerId, booking2FutureItem1Id, false);
        bookingController.updateStatus(ownerId, booking3FutureItem1Id, true);
        bookingController.updateStatus(ownerId, booking4Item2Id, true);

        List<BookingOutDto> listWaitingBookings = bookingController.getListByBooker(bookerId, BookingState.WAITING, 0, 10);

        assertThat(listWaitingBookings).asList().hasSize(1);
        assertThat(listWaitingBookings).asList().contains(bookingFirstFutureItem1);

        assertThat(Optional.of(listWaitingBookings.get(0))).hasValueSatisfying(
                booking -> AssertionsForClassTypes.assertThat(booking)
                        .hasFieldOrPropertyWithValue("id", booking.getId())
                        .hasFieldOrPropertyWithValue("start",
                                LocalDateTime.of(2030, 1, 2, 20, 11, 11))
                        .hasFieldOrPropertyWithValue("end",
                                LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                        .hasFieldOrPropertyWithValue("booker", booker)
                        .hasFieldOrPropertyWithValue("item", item1)
                        .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING));


    }

    /**
     * test get list of user's bookings with state REJECTED
     */
    @Test
    public void shouldGetListOfRejectedBookingsByBooker() {
        UserDto userDto1 = userController.create(userAlex1);
        UserDto userDto2 = userController.create(userOlga4);
        Long ownerId = userDto1.getId();
        Long bookerId = userDto2.getId();
        User owner = UserMapper.toUser(userDto1);
        User booker = UserMapper.toUser(userDto2);
        ItemDto itemDto1 = itemController.create(ownerId, screwDriver);
        Item item1 = ItemMapper.toItem(itemDto1, owner, null);
        itemController.create(ownerId, adultBike);

        bookingController.create(bookerId, bookingItem1Future);
        BookingOutDto bookingSecondFutureItem1 = bookingController.create(bookerId, bookingItem1Future2);
        BookingOutDto bookingThirdFutureItem1 = bookingController.create(bookerId, bookingItem1Future3);
        BookingOutDto bookingCurrentItem2 = bookingController.create(bookerId, bookingItem2);

        Long booking2FutureItem1Id = bookingSecondFutureItem1.getId();
        Long booking3FutureItem1Id = bookingThirdFutureItem1.getId();
        Long booking4Item2Id = bookingCurrentItem2.getId();

        BookingOutDto booking2RejectedItem1Future = bookingController.updateStatus(ownerId,
                booking2FutureItem1Id, false);
        bookingController.updateStatus(ownerId, booking3FutureItem1Id, true);
        bookingController.updateStatus(ownerId, booking4Item2Id, true);

        List<BookingOutDto> listFutureBookings = bookingController.getListByBooker(bookerId, BookingState.REJECTED, 0, 10);

        assertThat(listFutureBookings).asList().hasSize(1);
        assertThat(listFutureBookings).asList().contains(booking2RejectedItem1Future);

        assertThat(Optional.of(listFutureBookings.get(0))).hasValueSatisfying(
                booking -> AssertionsForClassTypes.assertThat(booking)
                        .hasFieldOrPropertyWithValue("id", booking.getId())
                        .hasFieldOrPropertyWithValue("start",
                                LocalDateTime.of(2030, 2, 2, 20, 11, 11))
                        .hasFieldOrPropertyWithValue("end",
                                LocalDateTime.of(2030, 3, 1, 1, 1, 1))
                        .hasFieldOrPropertyWithValue("booker", booker)
                        .hasFieldOrPropertyWithValue("item", item1)
                        .hasFieldOrPropertyWithValue("status", BookingStatus.REJECTED));

    }

    /**
     * get user's empty list by owner
     */

    @Test
    public void shouldGetEmptyBookingListByBooker() {

        UserDto userDto1 = userController.create(userOlga4);
        Long bookerId = userDto1.getId();

        List<BookingOutDto> listUsers = bookingController.getListByOwner(bookerId, BookingState.ALL,
                0, 10);

        assertThat(listUsers).asList().hasSize(0);
        assertThat(listUsers).asList().isEmpty();

    }

    /**
     * test fail add comments from user without bookings with UnavailableItemException
     */
    @Test
    public void shouldFailAddCommentsFromUserWithoutBookings() {

        UserDto userDto = userController.create(userAlex1);
        UserDto userDto1 = userController.create(userOlga4);
        Long ownerId = userDto.getId();
        Long notBookerId = userDto1.getId();
        ItemDto itemDto = itemController.create(ownerId, adultBike);

        assertThrows(UnavailableItemException.class,
                () -> itemController.addComment(notBookerId, commentToItem1First, itemDto.getId()),
                "Не выброшено исключение UnavailableItemException.");

    }

    /**
     * test fail add comments from owner with
     */
    @Test
    public void shouldFailAddCommentsFromOwner() {

        UserDto userDto = userController.create(userAlex1);
        Long ownerId = userDto.getId();
        ItemDto itemDto = itemController.create(ownerId, adultBike);

        assertThrows(AccessIsNotAllowedException.class,
                () -> itemController.addComment(ownerId, commentToItem1First, itemDto.getId()),
                "Не выброшено исключение UnsupportedStatusException.");

    }


    /**
     * test fail add comments from user with future booking
     */
    @Test
    public void shouldFailAddCommentsFromUserWithFutureBookings() {

        UserDto userDto = userController.create(userAlex1);
        UserDto userDto1 = userController.create(userOlga4);
        Long ownerId = userDto.getId();
        Long bookerId = userDto1.getId();
        ItemDto itemDto = itemController.create(ownerId, screwDriver);
        User user = UserMapper.toUser(userDto);
        Item item = ItemMapper.toItem(itemDto, user, null);
        Long itemId = item.getId();
        bookingController.create(bookerId, bookingItem1Future);
        Long bookingId = userDto.getId();
        bookingController.updateStatus(ownerId, bookingId, true);
        CommentDto commentDto = commentToItem1First;

        assertThrows(UnavailableItemException.class,
                () -> itemController.addComment(bookerId, commentDto, itemId),
                "Не выброшено исключение UnavailableItemException.");

    }

    /**
     * test fail add comments from user with rejected booking with UnavailableItemException
     */
    @Test
    public void shouldFailAddCommentsFromUserWithRejectedStatusBookings() {

        UserDto userDto = userController.create(userAlex1);
        UserDto userDto1 = userController.create(userOlga4);
        Long ownerId = userDto.getId();
        Long bookerId = userDto1.getId();
        ItemDto itemDto2 = itemController.create(ownerId, adultBike);
        itemController.create(ownerId, adultBike);
        User user = UserMapper.toUser(userDto);
        Item item = ItemMapper.toItem(itemDto2, user, null);
        Long itemId = item.getId();
        bookingController.create(bookerId, bookingItem2);
        Long bookingId = userDto.getId();
        bookingController.updateStatus(ownerId, bookingId, false);

        assertThrows(UnavailableItemException.class,
                () -> itemController.addComment(bookerId, commentToItem2, itemId),
                "Не выброшено исключение UnavailableItemException.");

    }


}
