package com.gymmanagement.gym.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class SubscriptionTest {

    @Test
    void prePersist_shouldSetCreatedAtAndUpdatedAt() {
        Subscription subscription = new Subscription();

        LocalDateTime before = LocalDateTime.now();

        subscription.prePersist();

        LocalDateTime after = LocalDateTime.now();

        assertThat(subscription.getCreatedAt()).isBetween(before, after);
        assertThat(subscription.getUpdatedAt()).isBetween(before, after);
    }

    @Test
    void preUpdate_shouldUpdateUpdatedAt() throws InterruptedException {
        Subscription subscription = new Subscription();

        subscription.prePersist();
        LocalDateTime firstUpdate = subscription.getUpdatedAt();

        // para asegurar una diferencia de tiempo
        Thread.sleep(5);

        subscription.preUpdate();

        assertThat(subscription.getUpdatedAt()).isAfter(firstUpdate);
    }

}
