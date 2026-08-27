package com.gymmanagement.gym.mapper;

import org.springframework.stereotype.Component;

import com.gymmanagement.gym.dto.MembershipDTO;
import com.gymmanagement.gym.entities.Membership;

@Component
public class MembershipMapper {

    public Membership toEntity(MembershipDTO dto) {
        Membership membership = new Membership();
        membership.setId(dto.getId());
        membership.setName(dto.getName());
        membership.setDurationMonths(dto.getDurationMonths());
        membership.setPrice(dto.getPrice());
        return membership;
    }

}
