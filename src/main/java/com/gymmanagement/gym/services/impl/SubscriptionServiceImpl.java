package com.gymmanagement.gym.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.entities.Membership;
import com.gymmanagement.gym.entities.Subscription;
import com.gymmanagement.gym.repository.SubscriptionRepository;
import com.gymmanagement.gym.services.SubscriptionService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public List<Subscription> findAll() {
        return subscriptionRepository.findAll();
    }

    @Override
    public Optional<Subscription> findByMemberAndStatusTrue(Member member) {
        return subscriptionRepository.findByMemberAndStatusTrue(member);
    }

    @Transactional
    public Subscription assign(Member member, Membership membership) {
        // si tenía una activa, la desactiva
        subscriptionRepository.findByMemberAndStatusTrue(member).ifPresent(sub -> {
            sub.setStatus(false);
            subscriptionRepository.save(sub);
        });
        Subscription subscription = new Subscription();
        subscription.setMember(member);
        subscription.setMembership(membership);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(LocalDate.now().plusMonths(membership.getDurationMonths()));
        return subscriptionRepository.save(subscription);
    }

    @Override
    public BigDecimal getTotalIncome() {
        return subscriptionRepository.sumAllIncome();
    }

    @Override
    public long countExpiredSubscriptions() {
        return subscriptionRepository.countByStatusTrueAndEndDateBefore(LocalDate.now());
    }

    @Override
    public String getMostSoldPlanName() {
        List<String> results = subscriptionRepository.findMostSoldPlanNames();
        return results.isEmpty() ? "Sin datos" : results.get(0);
    }

}
