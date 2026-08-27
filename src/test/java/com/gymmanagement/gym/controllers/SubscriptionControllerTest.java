package com.gymmanagement.gym.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.View;

import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.entities.Membership;
import com.gymmanagement.gym.entities.Subscription;
import com.gymmanagement.gym.services.MemberService;
import com.gymmanagement.gym.services.MembershipsService;
import com.gymmanagement.gym.services.SubscriptionService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class SubscriptionControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private MembershipsService membershipService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private SubscriptionController subscriptionController;

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

        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void list_shouldReturnMembershipsListView() throws Exception {
        when(membershipService.findAll()).thenReturn(List.of(new Membership()));

        mockMvc.perform(get("/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(view().name("memberships/list"))
                .andExpect(model().attribute("activePage", "subscriptions"));
    }

    @Test
    void showAssignForm_withoutDni_shouldNotSearchMember() throws Exception {
        when(membershipService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/subscriptions/assign"))
                .andExpect(status().isOk())
                .andExpect(view().name("memberships/list"))
                .andExpect(model().attributeDoesNotExist("member"));

        verifyNoInteractions(memberService);
    }

    @Test
    void showAssignForm_withExistingDniAndActiveSubscription_shouldShowMemberAndSubscription() throws Exception {
        Member member = new Member();
        member.setId(1L);
        member.setDni("70000025");

        Subscription subscription = new Subscription();
        subscription.setId(10L);

        when(membershipService.findAll()).thenReturn(List.of());
        when(memberService.findByDni("70000025")).thenReturn(Optional.of(member));
        when(subscriptionService.findByMemberAndStatusTrue(member)).thenReturn(Optional.of(subscription));

        mockMvc.perform(get("/subscriptions/assign").param("dni", "70000025"))
                .andExpect(status().isOk())
                .andExpect(view().name("memberships/list"))
                .andExpect(model().attribute("member", member))
                .andExpect(model().attribute("activeSubscription", subscription));
    }

    @Test
    void showAssignForm_withNonExistingDni_shouldShowError() throws Exception {
        when(membershipService.findAll()).thenReturn(List.of());
        when(memberService.findByDni("99999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/subscriptions/assign").param("dni", "99999999"))
                .andExpect(status().isOk())
                .andExpect(view().name("memberships/list"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeDoesNotExist("member"));
    }

    @Test
    void assign_shouldRegisterSubscriptionAndRedirect() throws Exception {
        Member member = new Member();
        member.setId(1L);
        member.setDni("70000025");

        Membership membership = new Membership();
        membership.setId(2L);

        when(memberService.findByDni("70000025")).thenReturn(Optional.of(member));
        when(membershipService.findById(2L)).thenReturn(Optional.of(membership));

        mockMvc.perform(post("/subscriptions/assign")
                        .param("dni", "70000025")
                        .param("membershipId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/subscriptions/assign?dni=70000025"));

        verify(subscriptionService).assign(member, membership);
    }

    @Test
    void assign_withNonExistingMember_shouldThrowException() {
        when(memberService.findByDni("99999999")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () ->
                mockMvc.perform(post("/subscriptions/assign")
                        .param("dni", "99999999")
                        .param("membershipId", "2")));

        verifyNoInteractions(subscriptionService);
    }

    @Test
    void assign_withNonExistingMembership_shouldThrowException() {
        Member member = new Member();
        member.setId(1L);
        member.setDni("70000025");

        when(memberService.findByDni("70000025")).thenReturn(Optional.of(member));
        when(membershipService.findById(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () ->
                mockMvc.perform(post("/subscriptions/assign")
                        .param("dni", "70000025")
                        .param("membershipId", "99")));

        verifyNoInteractions(subscriptionService);
    }

    @Test
    void showAssignForm_withBlankDni_shouldNotSearchMember() throws Exception {
        when(membershipService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/subscriptions/assign").param("dni", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("memberships/list"))
                .andExpect(model().attributeDoesNotExist("member"));

        verifyNoInteractions(memberService);
    }

}
