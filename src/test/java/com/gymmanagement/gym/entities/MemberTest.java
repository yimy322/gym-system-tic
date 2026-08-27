package com.gymmanagement.gym.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class MemberTest {

    @Test
    void prePersist_shouldSetRegistrationDate() {
        Member member = new Member();
        member.prePersist();
        assertThat(member.getRegistrationDate()).isEqualTo(LocalDate.now());
    }

}
