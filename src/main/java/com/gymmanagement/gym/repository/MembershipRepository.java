package com.gymmanagement.gym.repository;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gymmanagement.gym.entities.Membership;

public interface MembershipRepository extends JpaRepository<Membership,Long> {

    Optional<Membership> findByName(String name);

    @Query("SELECT COALESCE(AVG(m.price), 0) FROM Membership m")
    BigDecimal findAveragePrice();

}
