package com.gymmanagement.gym.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gymmanagement.gym.services.AttendanceService;
import com.gymmanagement.gym.services.MemberService;
import com.gymmanagement.gym.services.SubscriptionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final AttendanceService attendanceService;

    @GetMapping("/dashboard")
    public String index(Model model) {
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalIncome", subscriptionService.getTotalIncome());
        model.addAttribute("countActiveMembers", memberService.countByStatusTrue());
        model.addAttribute("countExpiredSubscriptions", subscriptionService.countExpiredSubscriptions());
        model.addAttribute("avgDailyAttendance", attendanceService.getAverageDailyAttendance());
        return "dashboard";
    }

}
