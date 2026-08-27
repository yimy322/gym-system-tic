package com.gymmanagement.gym.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gymmanagement.gym.entities.Member;
import com.gymmanagement.gym.repository.MemberRepository;
import com.gymmanagement.gym.services.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    @Override
    public Member save(Member member) {
        if(memberRepository.existsByDni(member.getDni())) {
            throw new RuntimeException("El DNI ya existe");
        }
        return memberRepository.save(member);
    }

    @Override
    public Member update(Long id, Member member) {
        Member memberDb = memberRepository.findById(id).orElseThrow(() -> new RuntimeException("Afiliado no encontrado"));
        memberDb.setFirstName(member.getFirstName());
        memberDb.setLastName(member.getLastName());
        memberDb.setPhone(member.getPhone());
        memberDb.setEmail(member.getEmail());
        memberDb.setStatus(member.getStatus());
        return memberRepository.save(memberDb);
    }

    @Override
    public void delete(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new RuntimeException("Afiliado no encontrado"));
        memberRepository.delete(member);
    }

    @Override
    public void toggleStatus(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new RuntimeException("Afiliado no encontrado"));
        member.setStatus(!member.getStatus());
        memberRepository.save(member);
    }

    @Override
    public Long countByStatusTrue() {
        return memberRepository.countByStatusTrue();
    }

    @Override
    public List<Member> searchMembers(String keyword) {
        return memberRepository.findByDniContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword, keyword);
    }

    @Override
    public Optional<Member> findByDni(String dni) {
        return memberRepository.findByDni(dni);
    }

}
