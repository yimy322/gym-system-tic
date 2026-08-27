package com.gymmanagement.gym.services;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import com.gymmanagement.gym.entities.Attendance;

public interface AttendanceService {

    Attendance registerAttendance(Long memberId);

    List<Attendance> findHistoryByMember(Long memberId);

    List<Attendance> findAll();

    List<Attendance> findByMemberAndMonth(Long memberId, YearMonth yearMonth);
    long countByMember(Long memberId);
    Optional<Attendance> findLastAttendance(Long memberId);

    double getAverageDailyAttendance();
}