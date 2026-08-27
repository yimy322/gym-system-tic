package com.gymmanagement.gym.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gymmanagement.gym.entities.Membership;


@Service
public interface MembershipsService {

    List<Membership> findAll();

    Membership save(Membership membresia);

    Membership update(Long id, Membership membership);

    Optional<Membership> findById(Long id);

    void delete(Long id);

    BigDecimal getAveragePrice();

}
