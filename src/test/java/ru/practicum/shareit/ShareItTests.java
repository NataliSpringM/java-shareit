package ru.practicum.shareit;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.util.exceptions.ConflictEmailException;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
class ShareItTests {
    @Autowired
    private UserController userController;
    @Autowired
    private ItemController itemController;

    UserDto userAlex1;
    UserDto userEgor2;
    UserDto userAlex3;
    UserDto userOlga4;
    ItemDto screwDriver;
    ItemDto lawnMower;
    ItemDto bike;
    ItemDto noName;

    ItemDto nullDescription;
    ItemDto nullAvailable;
    ItemDto onlyAvailable;
    ItemDto onlyDescription;
    ItemDto onlyName;

    @BeforeEach
    public void create() {

        userAlex1 = UserDto.builder().email("Alex@yandex.ru").name("Alexandr Ivanov").build();
        userEgor2 = UserDto.builder().email(" ").name("Egor Egorov").build();
        userAlex3 = UserDto.builder().email("Alex@yandex.ru").name("Alexey Petrov").build();
        userOlga4 = UserDto.builder().email("Olga@yandex.ru").name("Olga Smith").build();
        screwDriver = ItemDto.builder().name("screwdriver").description("new").available(true).build();
        lawnMower = ItemDto.builder().name("lawn-mower").description("portable").available(false).build();
        bike = ItemDto.builder().name("bike").description("for children").available(true).build();
        noName = ItemDto.builder().name("").description("for children").available(true).build();
        nullDescription = ItemDto.builder().name("bike").available(true).build();
        nullAvailable = ItemDto.builder().name("bike").description("adult").build();
        onlyAvailable = ItemDto.builder().available(false).build();
        onlyDescription = ItemDto.builder().description("patched description").build();
        onlyName = ItemDto.builder().name("updated").build();


    }

    /**
     * test create and get user with valid data
     */
    @Test
    public void shouldCreateUserAndGetUserById() { // создание пользователя и его возврат по id

        UserDto user1 = userController.create(userAlex1);
        Optional<UserDto> userOptional = Optional.ofNullable(userController.getById(user1.getId()));
        assertThat(userOptional).hasValueSatisfying(user -> assertThat(user)
                .hasFieldOrPropertyWithValue("id", user.getId())
                .hasFieldOrPropertyWithValue("email", "Alex@yandex.ru")
                .hasFieldOrPropertyWithValue("name", "Alexandr Ivanov"));
    }

    /**
     * test fail get user by invalid id
     */
    @Test
    public void shouldFailGetUserByInvalidId() { //

        final Long userId = -1L;
        assertThrows(ObjectNotFoundException.class,
                () -> userController.getById(userId),
                "Не выброшено исключение ObjectNotFoundException.");
    }

    /**
     * test fail create user with invalid email
     */
    @Test
    public void shouldFailCreateUserWithEmptyEmail() { // создание пользователя с пустым email

        assertThrows(ConstraintViolationException.class,
                () -> userController.create(userController.create(userEgor2)),
                "Не выброшено исключение ConstraintViolationException.");
    }

    /**
     * test fail create user with email registered by another user
     */
    @Test
    public void shouldFailCreateUserWithSameEmail() { // создание пользователя с существующим email

        assertThrows(ConflictEmailException.class,
                () -> userController.create(userController.create(userAlex3)),
                "Не выброшено исключение ConflictEmailException.");
    }

    /**
     * test update user
     */
    @Test
    public void shouldUpdateUser() { // обновление пользователя

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
    public void shouldListUsers() { // получение списка пользователей

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

    @Test
    public void shouldGetEmptyListUsers() { // получение пустого списка пользователей

        List<UserDto> listUsers = userController.getList();

        assertThat(listUsers).asList().hasSize(0);
        assertThat(listUsers).asList().isEmpty();

    }

    /**
     * test create and get item with valid data
     */
    @Test
    public void shouldCreateItemAndGetItById() { // добавление вещи
        UserDto userDto = userController.create(userAlex1);
        Long userId = userDto.getId();
        ItemDto itemDto = itemController.create(1L, screwDriver);

        Optional<ItemDto> itemOptional = Optional.ofNullable(itemController.getById(userId, itemDto.getId()));
        assertThat(itemOptional).hasValueSatisfying(item -> assertThat(item)
                .hasFieldOrPropertyWithValue("id", item.getId())
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("name", "screwdriver"));

    }

    /**
     * test fail create item with invalid name
     */
    @Test
    public void shouldFailCreateItemWithEmptyName() {
        userController.create(userAlex1);
        // создание вещи с пустым названием

        assertThrows(ConstraintViolationException.class,
                () -> itemController.create(1L, noName),
                "Не выброшено исключение ConstraintViolationException.");
    }

    /**
     * test fail create item with invalid description
     */
    @Test
    public void shouldFailCreateItemWithNullDescription() {
        userController.create(userAlex1);

        assertThrows(ConstraintViolationException.class,
                () -> itemController.create(1L, nullDescription),
                "Не выброшено исключение ConstraintViolationException.");
    }

    /**
     * test update item with all properties in Dto object
     */
    @Test
    public void shouldFailCreateItemWithNullAvailableField() {
        userController.create(userAlex1);
        assertThrows(ConstraintViolationException.class,
                () -> itemController.create(1L, nullAvailable),
                "Не выброшено исключение ConstraintViolationException.");
    }

    /**
     * test update item with all properties in Dto object
     */
    @Test
    public void shouldUpdateItem() { // добавление вещи
        UserDto userDto = userController.create(userAlex1);
        final Long userId = userDto.getId();
        ItemDto itemDto = itemController.create(userId, screwDriver);
        ItemDto updatedDescriptionItem = itemDto.toBuilder().description("rusty and old").build();
        itemController.update(userId, updatedDescriptionItem, itemDto.getId());

        Optional<ItemDto> itemOptional = Optional.ofNullable(itemController.getById(userId, itemDto.getId()));
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

        Optional<ItemDto> itemOptional = Optional.ofNullable(itemController.getById(userId, itemDto.getId()));
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

        Optional<ItemDto> itemOptional = Optional.ofNullable(itemController.getById(userId, itemDto.getId()));
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

        Optional<ItemDto> itemOptional = Optional.ofNullable(itemController.getById(userId, itemDto.getId()));
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
        final Long userId = userDto.getId();
        final Long itemId = itemDto.getId();
        List<ItemDto> listWithItem = itemController.getListByUser(userId);
        assertThat(listWithItem).asList().hasSize(1);
        assertThat(listWithItem).asList().contains(itemDto);

        itemController.delete(itemId);
        List<ItemDto> list = itemController.getListByUser(userId);
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

        List<ItemDto> listItems = itemController.getListByUser(user1Id);
        List<ItemDto> list2Items = itemController.getListByUser(user4Id);

        assertThat(listItems).asList().hasSize(2);

        assertThat(listItems).asList().contains(item1Dto);
        assertThat(listItems).asList().contains(item2Dto);

        assertThat(Optional.of(listItems.get(0))).hasValueSatisfying(
                user -> AssertionsForClassTypes.assertThat(user)
                        .hasFieldOrPropertyWithValue("available", true));

        assertThat(Optional.of(listItems.get(1))).hasValueSatisfying(
                user -> AssertionsForClassTypes.assertThat(user)
                        .hasFieldOrPropertyWithValue("available", false));

        assertThat(list2Items).asList().hasSize(1);
        assertThat(list2Items).asList().contains(item3Dto);
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

        List<ItemDto> listItems = itemController.searchItemsBySubstring("Er");

        assertThat(listItems).asList().hasSize(1);

        assertThat(listItems).asList().startsWith(itemController.getById(user1.getId(), item1Dto.getId()));
        assertThat(listItems).asList().doesNotContain(itemController.getById(user1.getId(), item2Dto.getId()));

        assertThat(Optional.of(listItems.get(0))).hasValueSatisfying(
                item -> AssertionsForClassTypes.assertThat(item)
                        .hasFieldOrPropertyWithValue("name", "screwdriver"));

        // получаем список доступных вещей, содержащих в названии или описании подстроку er без учета регистра
        // проверяем корректность полученных данных - 2 вещи,
        List<ItemDto> list2Items = itemController.searchItemsBySubstring("e");

        assertThat(list2Items).asList().hasSize(2);

        assertThat(list2Items).asList().contains(itemController.getById(user4.getId(), item3Dto.getId()));

        assertThat(Optional.of(list2Items.get(0)))
                .hasValueSatisfying(item -> AssertionsForClassTypes.assertThat(item)
                        .hasFieldOrPropertyWithValue("name", "screwdriver"));
        assertThat(Optional.of(list2Items.get(1)))
                .hasValueSatisfying(item -> AssertionsForClassTypes.assertThat(item)
                        .hasFieldOrPropertyWithValue("name", "bike"));

    }
}
