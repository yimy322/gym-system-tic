package com.gymmanagement.gym.controllers;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.gymmanagement.gym.dto.MembershipDTO;
import com.gymmanagement.gym.entities.Membership;
import com.gymmanagement.gym.mapper.MembershipMapper;
import com.gymmanagement.gym.services.MembershipsService;
import com.gymmanagement.gym.services.SubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipsService service;
    private final MembershipMapper membershipMapper;
    private final SubscriptionService subscriptionService;

    @GetMapping("/settings")
    public String settings(Model model) {
        List<Membership> memberships = service.findAll();

        model.addAttribute("memberships", memberships);
        model.addAttribute("membershipsSize", memberships.size());
        model.addAttribute("membership", new MembershipDTO());
        model.addAttribute("activePage", "settings");

        model.addAttribute("mostSoldPlan", subscriptionService.getMostSoldPlanName());
        model.addAttribute("averagePrice", service.getAveragePrice());

        return "memberships/settings";
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("memberships", service.findAll());
        model.addAttribute("activePage", "subscriptions");
        return "memberships/list";
    }

    // GUARDAR O ACTUALIZAR
    @PostMapping("/register")
    public String save(@Valid @ModelAttribute("membership") MembershipDTO membershipDTO,
                   BindingResult result,
                   Model model) {
        if(result.hasErrors()) {
            model.addAttribute("activePage", "settings");
            model.addAttribute("memberships", service.findAll());
            return "memberships/settings";
        }
        Membership membership = membershipMapper.toEntity(membershipDTO);
        if(membership.getId() == null) {
            service.save(membership);
        } else {
            service.update(membership.getId(), membership);
        }
        return "redirect:/memberships/settings";
    }

    // EDITAR
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Membership membership = service.findById(id).orElseThrow();
        model.addAttribute("membership", membership);
        model.addAttribute("activePage", "settings");
        model.addAttribute("memberships", service.findAll());
        return "memberships/settings";
    }

}
