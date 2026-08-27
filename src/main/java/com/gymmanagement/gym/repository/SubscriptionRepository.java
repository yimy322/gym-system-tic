package com.gymmanagement.gym.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.entities.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByMemberAndStatusTrue(Member member);

    @Query("SELECT COALESCE(SUM(s.membership.price), 0) FROM Subscription s")
    BigDecimal sumAllIncome();

    long countByStatusTrueAndEndDateBefore(LocalDate date);

    @Query("SELECT s.membership.name FROM Subscription s " +
           "GROUP BY s.membership.name " +
           "ORDER BY COUNT(s) DESC")
    List<String> findMostSoldPlanNames();

}