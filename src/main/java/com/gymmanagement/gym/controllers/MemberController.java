package com.gymmanagement.gym.controllers;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gymmanagement.gym.dto.MemberDTO;
import com.gymmanagement.gym.entities.Attendance;
import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.mapper.MemberMapper;
import com.gymmanagement.gym.services.AttendanceService;
import com.gymmanagement.gym.services.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;
    private final AttendanceService attendanceService;

    @GetMapping
    public String list(@RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {

        model.addAttribute("members", memberService.findAll());
        model.addAttribute("activePage", "members");

        YearMonth yearMonth = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("monthLabel", formatMonthLabel(yearMonth));
        model.addAttribute("calendarWeeks", buildCalendarGrid(yearMonth));
        model.addAttribute("today", LocalDate.now());

        if (memberId != null) {
            Member member = memberService.findById(memberId).orElseThrow();
            model.addAttribute("selectedMember", member);

            List<Attendance> monthAttendances = attendanceService.findByMemberAndMonth(memberId, yearMonth);
            Set<Integer> attendanceDays = monthAttendances.stream()
                    .map(a -> a.getAttendanceDate().getDayOfMonth())
                    .collect(Collectors.toSet());
            model.addAttribute("attendanceDays", attendanceDays);

            model.addAttribute("totalAttendances", attendanceService.countByMember(memberId));

            attendanceService.findLastAttendance(memberId)
                    .ifPresent(a -> model.addAttribute("lastAttendance", a.getAttendanceDate()));
        }
        return "members/list";
    }

    public String formatMonthLabel(YearMonth ym) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es"));
        String label = ym.format(formatter);
        return label.substring(0, 1).toUpperCase() + label.substring(1);
    }

    public List<List<Integer>> buildCalendarGrid(YearMonth ym) {
        List<List<Integer>> weeks = new ArrayList<>();
        LocalDate firstDay = ym.atDay(1);
        // DayOfWeek: LUNES=1 ... DOMINGO=7 -> queremos que la semana empiece en
        // DOMINGO=0
        int startOffset = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = ym.lengthOfMonth();

        List<Integer> currentWeek = new ArrayList<>();
        for (int i = 0; i < startOffset; i++) {
            currentWeek.add(0); // 0 = celda vacía
        }
        for (int day = 1; day <= daysInMonth; day++) {
            currentWeek.add(day);
            if (currentWeek.size() == 7) {
                weeks.add(currentWeek);
                currentWeek = new ArrayList<>();
            }
        }
        if (!currentWeek.isEmpty()) {
            while (currentWeek.size() < 7)
                currentWeek.add(0);
            weeks.add(currentWeek);
        }
        return weeks;
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("members", memberService.findAll());
        model.addAttribute("member", new MemberDTO());
        model.addAttribute("activePage", "membersRegistration");
        return "members/register";
    }

    @PostMapping("/register")
    public String save(@Valid @ModelAttribute("member") MemberDTO memberDTO,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "membersRegistration");
            model.addAttribute("members", memberService.findAll());
            return "members/register";
        }
        Member member = memberMapper.toEntity(memberDTO);
        if (member.getId() != null) {
            Member current = memberService.findById(member.getId()).orElseThrow();
            // seteamos el original por si acaso
            member.setDni(current.getDni());
        }
        if (member.getId() == null) {
            memberService.save(member);
        } else {
            memberService.update(member.getId(), member);
        }
        return "redirect:/members/register";// para volver a lamisma pantalla
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Member member = memberService.findById(id).orElseThrow();// si existe si o si por eso no validamos el optional
        model.addAttribute("member", member);
        model.addAttribute("members", memberService.findAll());
        model.addAttribute("activePage", "membersRegistration");
        return "members/register";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id) {
        memberService.toggleStatus(id);
        return "redirect:/members/register";
    }

    @GetMapping("/search")
    @ResponseBody
    public List<Member> search(@RequestParam String keyword) {
        return memberService.searchMembers(keyword);
    }

}
