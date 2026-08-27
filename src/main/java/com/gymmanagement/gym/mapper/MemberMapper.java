package com.gymmanagement.gym.mapper;

import org.springframework.stereotype.Component;

import com.gymmanagement.gym.dto.MemberDTO;
import com.gymmanagement.gym.entities.Member;

@Component
public class MemberMapper {

    public Member toEntity(MemberDTO dto) {
        Member member = new Member();
        member.setId(dto.getId());
        member.setDni(dto.getDni());
        member.setFirstName(dto.getFirstName());
        member.setLastName(dto.getLastName());
        member.setPhone(dto.getPhone());
        member.setEmail(dto.getEmail());
        return member;
    }

}
