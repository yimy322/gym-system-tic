package com.gymmanagement.gym.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gymmanagement.gym.entities.Attendance;
import com.gymmanagement.gym.entities.Member;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>{

    List<Attendance> findByMemberOrderByAttendanceDateDesc(Member member);

    Optional<Attendance> findByMemberAndAttendanceDate(Member member, LocalDate attendanceDate);

    List<Attendance> findByMemberAndAttendanceDateBetween(Member member, LocalDate start, LocalDate end);

    long countByMember(Member member);

    Optional<Attendance> findTopByMemberOrderByAttendanceDateDesc(Member member);

    long countByAttendanceDateBetween(LocalDate start, LocalDate end);

}
