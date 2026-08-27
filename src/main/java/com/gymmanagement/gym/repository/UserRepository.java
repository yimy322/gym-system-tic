package com.gymmanagement.gym.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gymmanagement.gym.entities.User;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByUsername(String username);
}
