package com.gymmanagement.gym.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.View;
import org.springframework.lang.NonNull;

import com.gymmanagement.gym.dto.MembershipDTO;
import com.gymmanagement.gym.entities.Membership;
import com.gymmanagement.gym.mapper.MembershipMapper;
import com.gymmanagement.gym.services.MembershipsService;
import com.gymmanagement.gym.services.SubscriptionService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MembershipControllerTest {

    @Mock
    private MembershipsService service;

    @Mock
    private MembershipMapper membershipMapper;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private MembershipController membershipController;

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

        mockMvc = MockMvcBuilders.standaloneSetup(membershipController)
                .setViewResolvers(viewResolver)
                .setValidator(new LocalValidatorFactoryBean()) // habilita @Valid real sobre MembershipDTO
                .build();
    }

    @Test
    void settings_shouldLoadAllDashboardMetrics() throws Exception {
        Membership membership = new Membership();
        membership.setId(1L);
        membership.setName("Mensual");

        when(service.findAll()).thenReturn(List.of(membership));
        when(subscriptionService.getMostSoldPlanName()).thenReturn("Mensual");
        when(service.getAveragePrice()).thenReturn(new BigDecimal("95.00"));

        mockMvc.perform(get("/memberships/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("memberships/settings"))
                .andExpect(model().attribute("activePage", "settings"))
                .andExpect(model().attribute("membershipsSize", 1))
                .andExpect(model().attribute("mostSoldPlan", "Mensual"))
                .andExpect(model().attribute("averagePrice", new BigDecimal("95.00")));
    }

    @Test
    void list_shouldReturnMembershipsListView() throws Exception {
        when(service.findAll()).thenReturn(List.of(new Membership()));

        mockMvc.perform(get("/memberships"))
                .andExpect(status().isOk())
                .andExpect(view().name("memberships/list"))
                .andExpect(model().attribute("activePage", "subscriptions"));
    }

    @Test
    void save_withValidData_shouldRedirectToSettings() throws Exception {
        Membership membership = new Membership();
        membership.setId(null);

        when(membershipMapper.toEntity(any(MembershipDTO.class))).thenReturn(membership);

        mockMvc.perform(post("/memberships/register")
                        .param("name", "Mensual")
                        .param("durationMonths", "1")
                        .param("price", "95.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/memberships/settings"));

        verify(service).save(membership);
        verify(service, never()).update(anyLong(), any());
    }

    @Test
    void save_withExistingId_shouldCallUpdate() throws Exception {
        Membership membership = new Membership();
        membership.setId(1L);

        when(membershipMapper.toEntity(any(MembershipDTO.class))).thenReturn(membership);

        mockMvc.perform(post("/memberships/register")
                        .param("id", "1")
                        .param("name", "Mensual")
                        .param("durationMonths", "1")
                        .param("price", "95.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/memberships/settings"));

        verify(service).update(1L, membership);
        verify(service, never()).save(any());
    }

    @Test
    void save_withInvalidData_shouldReturnSettingsViewWithErrors() throws Exception {
        when(service.findAll()).thenReturn(List.of());
        // se omiten campos obligatorios (name, durationMonths, price) para forzar errores de validacion
        mockMvc.perform(post("/memberships/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("memberships/settings"))
                .andExpect(model().attribute("activePage", "settings"));

        verify(service, never()).save(any());
        verify(service, never()).update(anyLong(), any());
    }

    @Test
    void edit_withExistingId_shouldReturnSettingsViewWithMembership() throws Exception {
        Membership membership = new Membership();
        membership.setId(1L);
        membership.setName("Mensual");

        when(service.findById(1L)).thenReturn(Optional.of(membership));
        when(service.findAll()).thenReturn(List.of(membership));

        mockMvc.perform(get("/memberships/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("memberships/settings"))
                .andExpect(model().attribute("membership", membership));
    }

}
