package com.gymmanagement.gym.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.repository.MemberRepository;
import com.gymmanagement.gym.services.impl.MemberServiceImpl;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setFirstName("Juan");
        member.setLastName("Perez");
        member.setDni("12345678");
    }

    // CREATE
    @Test
    void save_shouldSaveMember() {
        when(memberRepository.save(member)).thenReturn(member);
        Member result = memberService.save(member);
        assertNotNull(result);
        assertEquals(member.getId(), result.getId());
        verify(memberRepository, times(1)).save(member);
    }

    // READ ALL
    @Test
    void findAll_shouldReturnMemberList() {
        when(memberRepository.findAll()).thenReturn(List.of(member));
        List<Member> result = memberService.findAll();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(memberRepository, times(1)).findAll();
    }

    // READ BY ID
    @Test
    void findById_whenMemberExists_shouldReturnMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        Optional<Member> result = memberService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("Juan", result.get().getFirstName());
    }

    // UPDATE
    @Test
    void update_shouldUpdateMemberData() {
        Member data = new Member();
        data.setFirstName("Juan Editado");
        data.setLastName("Perez");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(data);
        Member result = memberService.update(1L, data);
        assertEquals("Juan Editado", result.getFirstName());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    // DELETE
    @Test
    void delete_shouldDeleteMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        memberService.delete(1L);
        verify(memberRepository, times(1)).findById(1L);
        verify(memberRepository, times(1)).delete(member);
    }

    // SAVE - rama DNI ya existe
    @Test
    void save_whenDniAlreadyExists_shouldThrowException() {
        when(memberRepository.existsByDni(member.getDni())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> memberService.save(member));

        assertEquals("El DNI ya existe", ex.getMessage());
        verify(memberRepository, never()).save(any());
    }

    // FIND BY ID - rama no existe
    @Test
    void findById_whenMemberNotFound_shouldReturnEmpty() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Member> result = memberService.findById(99L);

        assertTrue(result.isEmpty());
    }

    // UPDATE - rama no existe
    @Test
    void update_whenMemberNotFound_shouldThrowException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> memberService.update(99L, member));

        assertEquals("Afiliado no encontrado", ex.getMessage());
        verify(memberRepository, never()).save(any());
    }

    // DELETE - rama no existe
    @Test
    void delete_whenMemberNotFound_shouldThrowException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> memberService.delete(99L));

        assertEquals("Afiliado no encontrado", ex.getMessage());
        verify(memberRepository, never()).delete(any());
    }

    // TOGGLE STATUS - de activo a inactivo
    @Test
    void toggleStatus_whenMemberIsActive_shouldSetToInactive() {
        member.setStatus(true);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        memberService.toggleStatus(1L);

        assertFalse(member.getStatus());
        verify(memberRepository, times(1)).save(member);
    }

    // TOGGLE STATUS - de inactivo a activo
    @Test
    void toggleStatus_whenMemberIsInactive_shouldSetToActive() {
        member.setStatus(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        memberService.toggleStatus(1L);

        assertTrue(member.getStatus());
        verify(memberRepository, times(1)).save(member);
    }

    // TOGGLE STATUS - rama no existe
    @Test
    void toggleStatus_whenMemberNotFound_shouldThrowException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> memberService.toggleStatus(99L));

        assertEquals("Afiliado no encontrado", ex.getMessage());
        verify(memberRepository, never()).save(any());
    }

    // COUNT BY STATUS TRUE
    @Test
    void countByStatusTrue_shouldReturnCountFromRepository() {
        when(memberRepository.countByStatusTrue()).thenReturn(5L);

        Long result = memberService.countByStatusTrue();

        assertEquals(5L, result);
    }

    // SEARCH MEMBERS
    @Test
    void searchMembers_shouldReturnMatchingMembers() {
        when(memberRepository
                .findByDniContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        "juan", "juan", "juan"))
                .thenReturn(List.of(member));

        List<Member> result = memberService.searchMembers("juan");

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getFirstName());
    }

    // FIND BY DNI - existe
    @Test
    void findByDni_whenMemberExists_shouldReturnMember() {
        when(memberRepository.findByDni("12345678")).thenReturn(Optional.of(member));

        Optional<Member> result = memberService.findByDni("12345678");

        assertTrue(result.isPresent());
        assertEquals("12345678", result.get().getDni());
    }

    // FIND BY DNI - no existe
    @Test
    void findByDni_whenMemberNotFound_shouldReturnEmpty() {
        when(memberRepository.findByDni("00000000")).thenReturn(Optional.empty());

        Optional<Member> result = memberService.findByDni("00000000");

        assertTrue(result.isEmpty());
    }

}