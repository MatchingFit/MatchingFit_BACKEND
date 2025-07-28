package com.example.matching_fit.domain.user.repository;

import com.example.matching_fit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByRefreshToken(String refreshToken);
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByName(String name);

}
