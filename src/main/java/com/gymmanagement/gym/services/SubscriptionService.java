package com.gymmanagement.gym.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.entities.Membership;
import com.gymmanagement.gym.entities.Subscription;

public interface SubscriptionService {

    List<Subscription> findAll();

    Optional<Subscription> findByMemberAndStatusTrue(Member member);

    Subscription assign(Member member, Membership membership);

    BigDecimal getTotalIncome();
    long countExpiredSubscriptions();

    String getMostSoldPlanName();
   
}
