package com.example.matching_fit.domain.user.repository;

import com.example.matching_fit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByRefreshToken(String refreshToken);
    boolean existsByEmail(String email);
    boolean existsByName(String name);

    Optional<User> findByEmail(String email);

    Optional<User> findByName(String name);

}
