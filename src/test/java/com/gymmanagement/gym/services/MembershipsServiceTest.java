package com.gymmanagement.gym.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gymmanagement.gym.entities.Membership;
import com.gymmanagement.gym.repository.MembershipRepository;
import com.gymmanagement.gym.services.impl.MembershipsServiceImpl;

@ExtendWith(MockitoExtension.class)
public class MembershipsServiceTest {

    @Mock
    private MembershipRepository repository;

    @InjectMocks
    private MembershipsServiceImpl membershipsServiceImpl;

    private Membership membership;

    @BeforeEach
    void setUp() {
        membership = new Membership();
        membership.setId(1L);
        membership.setName("Plan Basico");
        membership.setDurationMonths(1);
        membership.setPrice(new BigDecimal("300.00"));
    }

    //FIND ALL
    @Test
    void findAll_shouldReturnAllMemberships() {
        when(repository.findAll()).thenReturn(List.of(membership));
        List<Membership> result = membershipsServiceImpl.findAll();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findAll_whenNoMemberships_shouldReturnEmptyList() {
        when(repository.findAll()).thenReturn(List.of());
        List<Membership> result = membershipsServiceImpl.findAll();
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAll();
    }

    //CREATE
    @Test
    void save_shouldSaveMembershipAndReturnIt() {
        when(repository.save(membership)).thenReturn(membership);
        Membership result = membershipsServiceImpl.save(membership);
        assertNotNull(result);
        assertEquals("Plan Basico", result.getName());
        assertEquals(new BigDecimal("300.00"), result.getPrice());
        verify(repository, times(1)).save(membership);
    }

    //READ BY ID
    @Test
    void findById_whenExists_shouldReturnMembership() {
        when(repository.findById(1L)).thenReturn(Optional.of(membership));
        Membership result = membershipsServiceImpl.findById(1L).get();
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Plan Basico", result.getName());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void findById_whenNotFound_shouldReturnNull() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        Optional<Membership> result = membershipsServiceImpl.findById(99L);
        assertTrue(result.isEmpty());
        verify(repository).findById(99L);
    }

    //DELETE
    @Test
    void delete_shouldCallRepositoryDeleteById() {
        doNothing().when(repository).deleteById(1L);
        membershipsServiceImpl.delete(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void update_whenMembershipExists_shouldUpdateAndReturnIt() {
        Membership data = new Membership();
        data.setName("Plan Premium");
        data.setDurationMonths(3);
        data.setPrice(new BigDecimal("700.00"));
        data.setStatus(true);

        when(repository.findById(1L)).thenReturn(Optional.of(membership));
        when(repository.save(any(Membership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Membership result = membershipsServiceImpl.update(1L, data);

        assertEquals("Plan Premium", result.getName());
        assertEquals(3, result.getDurationMonths());
        assertEquals(new BigDecimal("700.00"), result.getPrice());
        assertTrue(result.getStatus());
        verify(repository, times(1)).save(any(Membership.class));
    }

    @Test
    void update_whenMembershipNotFound_shouldThrowException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                membershipsServiceImpl.update(99L, membership));

        assertEquals("Membresia no encontrada", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void getAveragePrice_shouldReturnAverageFromRepository() {
        when(repository.findAveragePrice()).thenReturn(new BigDecimal("450.50"));

        BigDecimal result = membershipsServiceImpl.getAveragePrice();

        assertEquals(new BigDecimal("450.50"), result);
    }

}
