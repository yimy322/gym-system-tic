package com.gymmanagement.gym.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gymmanagement.gym.entities.Attendance;
import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.repository.AttendanceRepository;
import com.gymmanagement.gym.repository.MemberRepository;
import com.gymmanagement.gym.services.AttendanceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;

    @Override
    public Attendance registerAttendance(Long memberId) {
        // Buscar el afiliado por su identificador
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Afiliado no encontrado"));
        LocalDate today = LocalDate.now();
        // Validar que el afiliado no registre asistencia dos veces el mismo día
        if (attendanceRepository
                .findByMemberAndAttendanceDate(member, today)
                .isPresent()) {
            throw new RuntimeException("El afiliado ya registró asistencia el día de hoy");
        }
        // Crear el registro de asistencia
        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(LocalDateTime.now());
        // Guardar la asistencia en la base de datos
        return attendanceRepository.save(attendance);
    }

    @Override
    public List<Attendance> findHistoryByMember(Long memberId) {
        // Buscar el afiliado para consultar su historial
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Afiliado no encontrado"));
        return attendanceRepository.findByMemberOrderByAttendanceDateDesc(member);
    }

    @Override
    public List<Attendance> findAll() {
        return attendanceRepository.findAll();
    }

    @Override
    public List<Attendance> findByMemberAndMonth(Long memberId, YearMonth yearMonth) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Afiliado no encontrado"));
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return attendanceRepository.findByMemberAndAttendanceDateBetween(member, start, end);
    }

    @Override
    public long countByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Afiliado no encontrado"));
        return attendanceRepository.countByMember(member);
    }

    @Override
    public Optional<Attendance> findLastAttendance(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Afiliado no encontrado"));
        return attendanceRepository.findTopByMemberOrderByAttendanceDateDesc(member);
    }

    @Override
    public double getAverageDailyAttendance() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        long totalAttendances = attendanceRepository.countByAttendanceDateBetween(firstDayOfMonth, today);
        int daysElapsed = today.getDayOfMonth(); // dias transcurridos del mes, incluyendo hoy

        if (daysElapsed == 0) return 0;
        return (double) totalAttendances / daysElapsed;
    }

}
