package com.gymmanagement.gym.services;

import java.util.List;
import java.util.Optional;

import com.gymmanagement.gym.entities.Member;

public interface MemberService {

    List<Member> findAll();

    Optional<Member> findById(Long id);

    Optional<Member> findByDni(String dni);

    Member save(Member member);

    Member update(Long id, Member member);

    void delete(Long id);

    void toggleStatus(Long id);

    Long countByStatusTrue();

    List<Member> searchMembers(String keyword);

}
