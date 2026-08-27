package com.gymmanagement.gym.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.gymmanagement.gym.entities.Attendance;
import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.services.AttendanceService;
import com.gymmanagement.gym.services.MemberService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.lang.NonNull;

@ExtendWith(MockitoExtension.class)
public class AttendanceControllerTest {

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private AttendanceController attendanceController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        View stubView = new View() {
            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public void render(Map<String, ?> model, @NonNull jakarta.servlet.http.HttpServletRequest request,
                                @NonNull jakarta.servlet.http.HttpServletResponse response) {
                // no renderiza nada real, solo evita depender de Thymeleaf
            }
        };

        ViewResolver viewResolver = (viewName, locale) -> {
            if (viewName.startsWith("redirect:")) {
                return new RedirectView(viewName.substring("redirect:".length()));
            }
            return stubView;
        };

        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void list_sinDni_deberiaMostrarSoloElBuscador() throws Exception {
        mockMvc.perform(get("/attendance"))
                .andExpect(status().isOk())
                .andExpect(view().name("attendance"))
                .andExpect(model().attributeDoesNotExist("member"));

        verifyNoInteractions(memberService);
    }

    @Test
    void list_conDniExistente_deberiaMostrarMemberYAsistencias() throws Exception {
        // arrange
        Member member = new Member();
        member.setId(1L);
        member.setDni("70000025");
        member.setFirstName("Victor");
        member.setLastName("Acuña");

        Attendance attendance = new Attendance();
        attendance.setCheckInTime(LocalDateTime.now());

        when(memberService.findByDni("70000025")).thenReturn(Optional.of(member));
        when(attendanceService.findHistoryByMember(1L)).thenReturn(List.of(attendance));

        // act & assert
        mockMvc.perform(get("/attendance").param("dni", "70000025"))
                .andExpect(status().isOk())
                .andExpect(view().name("attendance"))
                .andExpect(model().attribute("member", member))
                .andExpect(model().attributeExists("attendances"));
    }

    @Test
    void list_conDniInexistente_deberiaMostrarError() throws Exception {
        when(memberService.findByDni("99999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/attendance").param("dni", "99999999"))
                .andExpect(status().isOk())
                .andExpect(view().name("attendance"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeDoesNotExist("member"));
    }

    @Test
    void mark_deberiaRegistrarAsistenciaYRedirigir() throws Exception {
        Member member = new Member();
        member.setId(1L);
        member.setDni("70000025");

        when(memberService.findByDni("70000025")).thenReturn(Optional.of(member));
        when(attendanceService.registerAttendance(1L)).thenReturn(new Attendance());

        mockMvc.perform(post("/attendance/mark").param("dni", "70000025"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/attendance?dni=70000025"));

        verify(attendanceService).registerAttendance(1L);
    }

    @Test
    void mark_cuandoYaRegistroHoy_deberiaRedirigirConError() throws Exception {
        Member member = new Member();
        member.setId(1L);
        member.setDni("70000025");

        when(memberService.findByDni("70000025")).thenReturn(Optional.of(member));
        when(attendanceService.registerAttendance(1L))
                .thenThrow(new RuntimeException("El afiliado ya registró asistencia el día de hoy"));

        mockMvc.perform(post("/attendance/mark").param("dni", "70000025"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/attendance?dni=70000025"));
    }

    @Test
    void mark_withNonExistingDni_shouldThrowException() {
        when(memberService.findByDni("99999999")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () ->
                mockMvc.perform(post("/attendance/mark").param("dni", "99999999")));

        verifyNoInteractions(attendanceService);
    }

    @Test
    void list_withBlankDni_shouldNotSearchMember() throws Exception {
        mockMvc.perform(get("/attendance").param("dni", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("attendance"))
                .andExpect(model().attributeDoesNotExist("member"));

        verifyNoInteractions(memberService);
    }

    @Test
    void list_withMoreThanThreeAttendances_shouldReturnOnlyFirstThree() throws Exception {
        Member member = new Member();
        member.setId(1L);
        member.setDni("70000025");

        List<Attendance> fourAttendances = List.of(
                new Attendance(), new Attendance(), new Attendance(), new Attendance()
        );

        when(memberService.findByDni("70000025")).thenReturn(Optional.of(member));
        when(attendanceService.findHistoryByMember(1L)).thenReturn(fourAttendances);

        mockMvc.perform(get("/attendance").param("dni", "70000025"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("attendances", fourAttendances.subList(0, 3)));
    }

}
