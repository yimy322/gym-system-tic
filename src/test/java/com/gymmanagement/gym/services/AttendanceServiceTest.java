package com.gymmanagement.gym.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gymmanagement.gym.entities.Attendance;
import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.repository.AttendanceRepository;
import com.gymmanagement.gym.repository.MemberRepository;
import com.gymmanagement.gym.services.impl.AttendanceServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceServiceImpl;

    private Member member;
    private Attendance attendance;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setFirstName("Juan");
        member.setLastName("Perez");

        attendance = new Attendance();
        attendance.setId(1L);
        attendance.setMember(member);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setCheckInTime(LocalDateTime.now());
    }

    //CREATE
    @Test
    void registerAttendance_whenMemberExistsAndNotRegisteredToday_shouldSaveAttendance() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attendanceRepository.findByMemberAndAttendanceDate(member, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        Attendance result = attendanceServiceImpl.registerAttendance(1L);
        assertNotNull(result);
        assertEquals(member, result.getMember());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    //CREATE
    @Test
    void registerAttendance_whenMemberNotFound_shouldThrowException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                attendanceServiceImpl.registerAttendance(99L));
        assertEquals("Afiliado no encontrado", ex.getMessage());
        verify(attendanceRepository, never()).save(any());
    }

    //CREATE
    @Test
    void registerAttendance_whenMemberAlreadyRegisteredToday_shouldThrowException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attendanceRepository.findByMemberAndAttendanceDate(member, LocalDate.now()))
                .thenReturn(Optional.of(attendance));
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                attendanceServiceImpl.registerAttendance(1L));
        assertEquals("El afiliado ya registró asistencia el día de hoy", ex.getMessage());
        verify(attendanceRepository, never()).save(any());
    }

    //FIND
    @Test
    void findHistoryByMember_whenMemberExists_shouldReturnHistory() {
        List<Attendance> history = List.of(attendance);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attendanceRepository.findByMemberOrderByAttendanceDateDesc(member))
                .thenReturn(history);
        List<Attendance> result = attendanceServiceImpl.findHistoryByMember(1L);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(attendanceRepository, times(1))
                .findByMemberOrderByAttendanceDateDesc(member);
    }

    @Test
    void findHistoryByMember_whenMemberNotFound_shouldThrowException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                attendanceServiceImpl.findHistoryByMember(99L));
        assertEquals("Afiliado no encontrado", ex.getMessage());
        verify(attendanceRepository, never())
                .findByMemberOrderByAttendanceDateDesc(any());
    }

    //READ ALL
    @Test
    void findAll_shouldReturnAllAttendances() {
        when(attendanceRepository.findAll()).thenReturn(List.of(attendance));
        List<Attendance> result = attendanceServiceImpl.findAll();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(attendanceRepository, times(1)).findAll();
    }

    @Test
    void findByMemberAndMonth_whenMemberExists_shouldReturnAttendancesInRange() {
        YearMonth yearMonth = YearMonth.of(2026, 7);
        List<Attendance> monthAttendances = List.of(attendance);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attendanceRepository.findByMemberAndAttendanceDateBetween(
                member, yearMonth.atDay(1), yearMonth.atEndOfMonth()))
                .thenReturn(monthAttendances);

        List<Attendance> result = attendanceServiceImpl.findByMemberAndMonth(1L, yearMonth);

        assertEquals(1, result.size());
        verify(attendanceRepository, times(1))
                .findByMemberAndAttendanceDateBetween(member, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    @Test
    void findByMemberAndMonth_whenMemberNotFound_shouldThrowException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                attendanceServiceImpl.findByMemberAndMonth(99L, YearMonth.of(2026, 7)));

        assertEquals("Afiliado no encontrado", ex.getMessage());
        verify(attendanceRepository, never()).findByMemberAndAttendanceDateBetween(any(), any(), any());
    }

    @Test
    void countByMember_whenMemberExists_shouldReturnCount() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attendanceRepository.countByMember(member)).thenReturn(7L);

        long result = attendanceServiceImpl.countByMember(1L);

        assertEquals(7L, result);
    }

    @Test
    void countByMember_whenMemberNotFound_shouldThrowException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                attendanceServiceImpl.countByMember(99L));

        assertEquals("Afiliado no encontrado", ex.getMessage());
        verify(attendanceRepository, never()).countByMember(any());
    }

    @Test
    void findLastAttendance_whenMemberExistsAndHasAttendance_shouldReturnLastOne() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attendanceRepository.findTopByMemberOrderByAttendanceDateDesc(member))
                .thenReturn(Optional.of(attendance));

        Optional<Attendance> result = attendanceServiceImpl.findLastAttendance(1L);

        assertTrue(result.isPresent());
        assertEquals(attendance, result.get());
    }

    @Test
    void findLastAttendance_whenMemberExistsButHasNoAttendance_shouldReturnEmpty() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attendanceRepository.findTopByMemberOrderByAttendanceDateDesc(member))
                .thenReturn(Optional.empty());

        Optional<Attendance> result = attendanceServiceImpl.findLastAttendance(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findLastAttendance_whenMemberNotFound_shouldThrowException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                attendanceServiceImpl.findLastAttendance(99L));

        assertEquals("Afiliado no encontrado", ex.getMessage());
        verify(attendanceRepository, never()).findTopByMemberOrderByAttendanceDateDesc(any());
    }

    @Test
    void getAverageDailyAttendance_shouldCalculateAverageCorrectly() {
        when(attendanceRepository.countByAttendanceDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(70L);

        double result = attendanceServiceImpl.getAverageDailyAttendance();

        int expectedDaysElapsed = LocalDate.now().getDayOfMonth();
        double expected = 70.0 / expectedDaysElapsed;
        assertEquals(expected, result, 0.0001); // delta para comparacion de doubles
    }

    @Test
    void getAverageDailyAttendance_shouldReturnZero_whenNoAttendances() {
        when(attendanceRepository.countByAttendanceDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0L);

        double result = attendanceServiceImpl.getAverageDailyAttendance();

        assertEquals(0.0, result, 0.0001);
    }

}
