package com.gymmanagement.gym.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.entities.Membership;
import com.gymmanagement.gym.entities.Subscription;
import com.gymmanagement.gym.repository.MemberRepository;
import com.gymmanagement.gym.repository.MembershipRepository;
import com.gymmanagement.gym.repository.SubscriptionRepository;
import com.gymmanagement.gym.services.impl.SubscriptionServiceImpl;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionServiceImpl;

    private Member member;
    private Membership membership;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setFirstName("Juan");
        membership = new Membership();
        membership.setId(1L);
        membership.setName("Plan Basico");
        membership.setDurationMonths(1);
        subscription = new Subscription();
        subscription.setId(1L);
        subscription.setMember(member);
        subscription.setMembership(membership);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(LocalDate.now().plusMonths(1));
        subscription.setStatus(true);
    }

    //READ ALL
    @Test
    void findAll_shouldReturnAllSubscriptions() {
        when(subscriptionRepository.findAll()).thenReturn(List.of(subscription));
        List<Subscription> result = subscriptionServiceImpl.findAll();
        assertFalse(result.isEmpty());
        verify(subscriptionRepository, times(1)).findAll();
    }

    @Test
    void findByMemberAndStatusTrue_shouldReturnActiveSubscription_whenExists() {
        when(subscriptionRepository.findByMemberAndStatusTrue(member))
                .thenReturn(Optional.of(subscription));

        Optional<Subscription> result = subscriptionServiceImpl.findByMemberAndStatusTrue(member);

        assertTrue(result.isPresent());
        assertEquals(subscription, result.get());
    }

    @Test
    void findByMemberAndStatusTrue_shouldReturnEmpty_whenNoActiveSubscription() {
        when(subscriptionRepository.findByMemberAndStatusTrue(member))
                .thenReturn(Optional.empty());

        Optional<Subscription> result = subscriptionServiceImpl.findByMemberAndStatusTrue(member);

        assertTrue(result.isEmpty());
    }

    @Test
    void assign_shouldDeactivatePreviousSubscription_whenMemberHasActiveOne() {
        Subscription previousActive = new Subscription();
        previousActive.setId(99L);
        previousActive.setStatus(true);

        when(subscriptionRepository.findByMemberAndStatusTrue(member))
                .thenReturn(Optional.of(previousActive));
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subscription result = subscriptionServiceImpl.assign(member, membership);

        // la anterior debe haberse desactivado y guardado
        assertFalse(previousActive.getStatus());
        verify(subscriptionRepository).save(previousActive);

        // la nueva debe estar correctamente armada
        assertEquals(member, result.getMember());
        assertEquals(membership, result.getMembership());
        assertEquals(LocalDate.now(), result.getStartDate());
        assertEquals(LocalDate.now().plusMonths(membership.getDurationMonths()), result.getEndDate());

        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
    }

    @Test
    void assign_shouldCreateNewSubscription_whenMemberHasNoActiveOne() {
        when(subscriptionRepository.findByMemberAndStatusTrue(member))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subscription result = subscriptionServiceImpl.assign(member, membership);

        assertEquals(member, result.getMember());
        assertEquals(membership, result.getMembership());
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    void getTotalIncome_shouldReturnSumFromRepository() {
        when(subscriptionRepository.sumAllIncome()).thenReturn(new BigDecimal("1500.00"));
        BigDecimal result = subscriptionServiceImpl.getTotalIncome();
        assertThat(result).isEqualByComparingTo("1500.00");
    }

    @Test
    void countExpiredSubscriptions_shouldReturnCountFromRepository() {
        when(subscriptionRepository.countByStatusTrueAndEndDateBefore(any(LocalDate.class)))
                .thenReturn(3L);

        long result = subscriptionServiceImpl.countExpiredSubscriptions();
        assertEquals(3L, result);
    }

    @Test
    void getMostSoldPlanName_shouldReturnFirstResult_whenResultsExist() {
        when(subscriptionRepository.findMostSoldPlanNames())
                .thenReturn(List.of("Plan Basico", "Plan Premium"));
        String result = subscriptionServiceImpl.getMostSoldPlanName();
        assertEquals("Plan Basico", result);
    }

    @Test
    void getMostSoldPlanName_shouldReturnDefaultMessage_whenNoResults() {
        when(subscriptionRepository.findMostSoldPlanNames())
                .thenReturn(List.of());
        String result = subscriptionServiceImpl.getMostSoldPlanName();
        assertEquals("Sin datos", result);
    }

}
