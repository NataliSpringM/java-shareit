package ru.practicum.shareit.user.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

/**
 * JpaRepository for users
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
}