package com.gymmanagement.gym.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MembershipDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "La duracion no puede ser nulo")
    private Integer durationMonths;

    @NotNull(message = "El precio es obligatorio")
    private BigDecimal price;

}
