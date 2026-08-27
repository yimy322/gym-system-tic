package com.gymmanagement.gym.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.gymmanagement.gym.dto.MembershipDTO;
import com.gymmanagement.gym.entities.Membership;

public class MembershipMapperTest {

    private final MembershipMapper mapper = new MembershipMapper();

    @Test
    void toEntity_shouldMapAllFields() {
        MembershipDTO dto = new MembershipDTO();
        dto.setId(2L);
        dto.setName("Mensual");
        dto.setDurationMonths(1);
        dto.setPrice(new BigDecimal("95.00"));

        Membership membership = mapper.toEntity(dto);

        assertThat(membership.getId()).isEqualTo(2L);
        assertThat(membership.getName()).isEqualTo("Mensual");
        assertThat(membership.getDurationMonths()).isEqualTo(1);
        assertThat(membership.getPrice()).isEqualByComparingTo("95.00");
    }

    @Test
    void toEntity_shouldMapNullId_whenItIsANewMembership() {
        MembershipDTO dto = new MembershipDTO();
        dto.setId(null);
        dto.setName("Anual");
        dto.setDurationMonths(12);
        dto.setPrice(new BigDecimal("700.00"));

        Membership membership = mapper.toEntity(dto);

        assertThat(membership.getId()).isNull();
        assertThat(membership.getDurationMonths()).isEqualTo(12);
    }

}
