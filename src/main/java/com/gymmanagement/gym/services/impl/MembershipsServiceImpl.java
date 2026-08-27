package com.gymmanagement.gym.services.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gymmanagement.gym.entities.Membership;
import com.gymmanagement.gym.repository.MembershipRepository;
import com.gymmanagement.gym.services.MembershipsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipsServiceImpl implements MembershipsService {

    private final MembershipRepository repository;

    @Override
    public List<Membership> findAll() {
        return repository.findAll();
    }

    @Override
    public Membership save(Membership membresia) {
        return repository.save(membresia);
    }

    @Override
    public Optional<Membership> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Membership update(Long id, Membership membership) {
        Membership membershipDb = repository.findById(id).orElseThrow(() -> new RuntimeException("Membresia no encontrada"));
        membershipDb.setName(membership.getName());
        membershipDb.setDurationMonths(membership.getDurationMonths());
        membershipDb.setPrice(membership.getPrice());
        membershipDb.setStatus(membership.getStatus());
        return repository.save(membershipDb);
    }

    @Override
    public BigDecimal getAveragePrice() {
        return repository.findAveragePrice();
    }
}
