package com.gymmanagement.gym.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.entities.Membership;
import com.gymmanagement.gym.services.MemberService;
import com.gymmanagement.gym.services.MembershipsService;
import com.gymmanagement.gym.services.SubscriptionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final MembershipsService membershipService;
    private final MemberService memberService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("memberships", membershipService.findAll());
        model.addAttribute("activePage", "subscriptions");
        return "memberships/list";
    }

    @GetMapping("/assign")
    public String showAssignForm(@RequestParam(required = false) String dni, Model model) {
        model.addAttribute("memberships", membershipService.findAll());
        model.addAttribute("activePage", "subscriptions");

        if (dni != null && !dni.isBlank()) {
            model.addAttribute("dni", dni);
            memberService.findByDni(dni).ifPresentOrElse(member -> {
                model.addAttribute("member", member);
                subscriptionService.findByMemberAndStatusTrue(member)
                        .ifPresent(sub -> model.addAttribute("activeSubscription", sub));
            }, () -> model.addAttribute("error", "No se encontró un afiliado con DNI " + dni));
        }
        return "memberships/list";
    }

    @PostMapping("/assign")
    public String assign(@RequestParam String dni,
                          @RequestParam Long membershipId,
                          RedirectAttributes redirectAttributes) {
        Member member = memberService.findByDni(dni)
                .orElseThrow(() -> new IllegalArgumentException("Afiliado no encontrado"));
        Membership membership = membershipService.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Membresía no encontrada"));
        subscriptionService.assign(member, membership);
        redirectAttributes.addFlashAttribute("success", "Membresía asignada correctamente");
        redirectAttributes.addAttribute("dni", dni);
        return "redirect:/subscriptions/assign";
    }

}
