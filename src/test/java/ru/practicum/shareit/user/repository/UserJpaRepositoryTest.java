package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * UserRepository test
 */
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserJpaRepositoryTest {
    @Autowired
    private UserRepository userRepository;


    /**
     * test findUserByEmailAndIdIsNot_shouldFindConflictEmailUser method
     * should find user with the same email and id differs from a given user id
     */
    @Test
    void findUserByEmailAndIdIsNot_shouldFindConflictEmailUser() {

        //save user with email
        Long id = 1L;
        String email = "Alex@yandex.ru";
        User user = User.builder()
                .id(id)
                .name("Alex")
                .email(email)
                .build();
        userRepository.save(user);

        //create otherUserId
        Long otherId = 2L;

        //invoke find method with other user's id
        Optional<User> userWithSameEmail = userRepository.findByEmailAndIdIsNot(email, otherId);

        //check found user
        assertThat(userWithSameEmail).hasValueSatisfying(opt -> assertThat(opt)
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", "Alex")
                .hasFieldOrPropertyWithValue("email", email));

    }

    /**
     * test findUserByEmailAndIdIsNot_shouldFindConflictEmailUser method
     * should not find user with the same email and id differs from a given user id if it doesn't exist
     */
    @Test
    void findUserByEmailAndIdIsNot_shouldNotFindConflictEmailUser() {

        //save user
        Long id = 1L;
        String email = "Alex@yandex.ru";
        User user = User.builder()
                .id(id)
                .name("Alex")
                .email(email)
                .build();
        userRepository.save(user);

        //invoke find method with user's id
        Optional<User> userWithSameEmail = userRepository.findByEmailAndIdIsNot(email, id);

        //check user not found
        assertEquals(userWithSameEmail, Optional.empty());
    }
}
