package com.gymmanagement.gym.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanagement.gym.entities.Attendance;
import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.services.AttendanceService;
import com.gymmanagement.gym.services.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final MemberService memberService;

    @GetMapping
    public String list(@RequestParam(required = false) String dni, Model model) {
        model.addAttribute("activePage", "attendance");
        if (dni != null && !dni.isBlank()) {
            model.addAttribute("dni", dni);
            memberService.findByDni(dni).ifPresentOrElse(member -> {
                model.addAttribute("member", member);
                List<Attendance> history = attendanceService.findHistoryByMember(member.getId());
                model.addAttribute("attendances", history.size() > 3 ? history.subList(0, 3) : history);
            }, () -> model.addAttribute("error", "No se encontró un afiliado con DNI " + dni));
        }
        return "attendance";
    }

    @PostMapping("/mark")
    public String mark(@RequestParam String dni, RedirectAttributes redirectAttributes) {
        Member member = memberService.findByDni(dni)
                .orElseThrow(() -> new IllegalArgumentException("Afiliado no encontrado"));
        try {
            attendanceService.registerAttendance(member.getId());
            redirectAttributes.addFlashAttribute("success", "Asistencia registrada correctamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        redirectAttributes.addAttribute("dni", dni);
        return "redirect:/attendance";
    }

}
