package com.gymmanagement.gym.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class AttendanceTest {

    @Test
    void prePersist_shouldSetRegistrationDate() {
        Attendance attendance = new Attendance();
        attendance.prePersist();
        assertThat(attendance.getCreatedAt()).isNotNull();
    }

}
