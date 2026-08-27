package com.gymmanagement.gym.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.gymmanagement.gym.dto.MemberDTO;
import com.gymmanagement.gym.entities.Member;

public class MemberMapperTest {

    private final MemberMapper mapper = new MemberMapper();

    @Test
    void toEntity_shouldMapAllFields() {
        MemberDTO dto = new MemberDTO();
        dto.setId(1L);
        dto.setDni("70000025");
        dto.setFirstName("Victor");
        dto.setLastName("Acuña Medina");
        dto.setPhone("999888777");
        dto.setEmail("victor@example.com");

        Member member = mapper.toEntity(dto);

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getDni()).isEqualTo("70000025");
        assertThat(member.getFirstName()).isEqualTo("Victor");
        assertThat(member.getLastName()).isEqualTo("Acuña Medina");
        assertThat(member.getPhone()).isEqualTo("999888777");
        assertThat(member.getEmail()).isEqualTo("victor@example.com");
    }

    @Test
    void toEntity_shouldMapNullId_whenItIsANewAffiliate() {
        MemberDTO dto = new MemberDTO();
        dto.setId(null);
        dto.setDni("70000099");
        dto.setFirstName("Ana");
        dto.setLastName("Torres");
        
        Member member = mapper.toEntity(dto);
        
        assertThat(member.getId()).isNull();
        assertThat(member.getDni()).isEqualTo("70000099");
    }

}
