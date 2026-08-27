package com.gymmanagement.gym.controllers;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.lang.NonNull;

import com.gymmanagement.gym.services.AttendanceService;
import com.gymmanagement.gym.services.MemberService;
import com.gymmanagement.gym.services.SubscriptionService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class DashboardControllerTest {

    @Mock
    private MemberService memberService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private DashboardController dashboardController;

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
                // no renderiza nada real
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setSingleView(stubView)
                .build();
    }

    @Test
    void index_deberiaCargarTodasLasMetricasDelDashboard() throws Exception {
        when(subscriptionService.getTotalIncome()).thenReturn(new BigDecimal("10580.00"));
        when(memberService.countByStatusTrue()).thenReturn(42L);
        when(subscriptionService.countExpiredSubscriptions()).thenReturn(5L);
        when(attendanceService.getAverageDailyAttendance()).thenReturn(3.65);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("activePage", "dashboard"))
                .andExpect(model().attribute("totalIncome", new BigDecimal("10580.00")))
                .andExpect(model().attribute("countActiveMembers", 42L))
                .andExpect(model().attribute("countExpiredSubscriptions", 5L))
                .andExpect(model().attribute("avgDailyAttendance", 3.65));
    }

}
