package com.gymmanagement.gym.controllers;

import com.gymmanagement.gym.entities.Attendance;
import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.mapper.MemberMapper;
import com.gymmanagement.gym.services.AttendanceService;
import com.gymmanagement.gym.services.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private MemberController memberController;

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

        ViewResolver viewResolver = (viewName, locale) -> {
            if (viewName.startsWith("redirect:")) {
                return new RedirectView(viewName.substring("redirect:".length()));
            }
            return stubView;
        };

        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .setViewResolvers(viewResolver)
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    // ---------- list ----------

    @Test
    void list_withoutMemberId_shouldNotLoadAttendanceData() throws Exception {
        when(memberService.findAll()).thenReturn(List.of(new Member()));

        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/list"))
                .andExpect(model().attribute("activePage", "members"))
                .andExpect(model().attributeDoesNotExist("selectedMember"));

        verifyNoInteractions(attendanceService);
    }

    @Test
    void list_withMemberId_shouldLoadSelectedMemberAndAttendance() throws Exception {
        Member member = new Member();
        member.setId(1L);
        member.setFirstName("Victor");
        member.setLastName("Acuña");

        Attendance attendance = new Attendance();
        attendance.setAttendanceDate(LocalDate.now());

        when(memberService.findAll()).thenReturn(List.of(member));
        when(memberService.findById(1L)).thenReturn(Optional.of(member));
        when(attendanceService.findByMemberAndMonth(eq(1L), any(YearMonth.class)))
                .thenReturn(List.of(attendance));
        when(attendanceService.countByMember(1L)).thenReturn(5L);
        when(attendanceService.findLastAttendance(1L)).thenReturn(Optional.of(attendance));

        mockMvc.perform(get("/members").param("memberId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/list"))
                .andExpect(model().attribute("selectedMember", member))
                .andExpect(model().attribute("totalAttendances", 5L))
                .andExpect(model().attributeExists("attendanceDays"))
                .andExpect(model().attributeExists("lastAttendance"));
    }

    @Test
    void list_withYearAndMonth_shouldUseGivenYearMonth() throws Exception {
        when(memberService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/members").param("year", "2026").param("month", "5"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("yearMonth", YearMonth.of(2026, 5)))
                .andExpect(model().attribute("monthLabel", "Mayo 2026"));
    }

    // ---------- helpers (formatMonthLabel / buildCalendarGrid) ----------

    @Test
    void formatMonthLabel_shouldCapitalizeFirstLetter() {
        String label = memberController.formatMonthLabel(YearMonth.of(2026, 7));
        assertThat(label).isEqualTo("Julio 2026");
    }

    @Test
    void buildCalendarGrid_shouldReturnCorrectNumberOfWeeksAndDays() {
        // julio 2026 empieza miercoles, tiene 31 dias
        List<List<Integer>> weeks = memberController.buildCalendarGrid(YearMonth.of(2026, 7));

        assertThat(weeks).isNotEmpty();
        // cada semana debe tener exactamente 7 columnas
        weeks.forEach(week -> assertThat(week).hasSize(7));

        // el total de dias no-cero debe ser igual a los dias del mes
        long totalDaysMarked = weeks.stream()
                .flatMap(List::stream)
                .filter(day -> day != 0)
                .count();
        assertThat(totalDaysMarked).isEqualTo(31);
    }

    @Test
    void buildCalendarGrid_shouldPadFirstWeekWithZeros() {
        // febrero 2026 empieza domingo (offset 0), por lo que la primer semana no deberia tener ceros
        List<List<Integer>> weeks = memberController.buildCalendarGrid(YearMonth.of(2026, 2));
        assertThat(weeks.get(0).get(0)).isNotEqualTo(0);
    }

    // ---------- register ----------

    @Test
    void register_shouldReturnRegisterViewWithEmptyDto() throws Exception {
        when(memberService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/members/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/register"))
                .andExpect(model().attribute("activePage", "membersRegistration"))
                .andExpect(model().attributeExists("member"));
    }

    // ---------- save ----------

    @Test
    void save_withValidNewMember_shouldSaveAndRedirect() throws Exception {
        Member member = new Member();
        member.setId(null);
        member.setDni("70000025");

        when(memberMapper.toEntity(any())).thenReturn(member);

        mockMvc.perform(post("/members/register")
                        .param("dni", "70000025")
                        .param("firstName", "Victor")
                        .param("lastName", "Acuña")
                        .param("phone", "999888777")
                        .param("email", "victor@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members/register"));

        verify(memberService).save(member);
        verify(memberService, never()).update(anyLong(), any());
    }

    @Test
    void save_withExistingMember_shouldKeepOriginalDniAndUpdate() throws Exception {
        Member current = new Member();
        current.setId(1L);
        current.setDni("70000025"); // dni original, no debe cambiar

        Member mapped = new Member();
        mapped.setId(1L);
        mapped.setDni("00000000"); // dni "hackeado" que llega del form, debe ser ignorado

        when(memberMapper.toEntity(any())).thenReturn(mapped);
        when(memberService.findById(1L)).thenReturn(Optional.of(current));

        mockMvc.perform(post("/members/register")
                        .param("id", "1")
                        .param("dni", "00000000")
                        .param("firstName", "Victor")
                        .param("lastName", "Acuña")
                        .param("phone", "999888777")
                        .param("email", "victor@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members/register"));

        verify(memberService).update(1L, mapped);
        assertThat(mapped.getDni()).isEqualTo("70000025"); // se restauro el dni original
        verify(memberService, never()).save(any());
    }

    @Test
    void save_withInvalidData_shouldReturnRegisterViewWithErrors() throws Exception {
        when(memberService.findAll()).thenReturn(List.of());

        // se omiten campos obligatorios para forzar errores de validacion
        mockMvc.perform(post("/members/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/register"))
                .andExpect(model().attribute("activePage", "membersRegistration"));

        verify(memberService, never()).save(any());
        verify(memberService, never()).update(anyLong(), any());
    }

    // ---------- edit ----------

    @Test
    void edit_withExistingId_shouldReturnRegisterViewWithMember() throws Exception {
        Member member = new Member();
        member.setId(1L);
        member.setDni("70000025");

        when(memberService.findById(1L)).thenReturn(Optional.of(member));
        when(memberService.findAll()).thenReturn(List.of(member));

        mockMvc.perform(get("/members/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/register"))
                .andExpect(model().attribute("member", member));
    }

    // ---------- toggleStatus ----------

    @Test
    void toggleStatus_shouldCallServiceAndRedirect() throws Exception {
        mockMvc.perform(get("/members/toggle-status/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members/register"));

        verify(memberService).toggleStatus(1L);
    }

    // ---------- search ----------

    @Test
    void search_shouldReturnMatchingMembersAsJson() throws Exception {
        Member member = new Member();
        member.setId(1L);
        member.setFirstName("Victor");

        when(memberService.searchMembers("vic")).thenReturn(List.of(member));

        mockMvc.perform(get("/members/search").param("keyword", "vic"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].firstName").value("Victor"));
    }

    @Test
    void list_withOnlyYearProvided_shouldFallBackToCurrentYearMonth() throws Exception {
        when(memberService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/members").param("year", "2026")) // sin month
                .andExpect(status().isOk())
                .andExpect(model().attribute("yearMonth", YearMonth.now()));
    }

}
