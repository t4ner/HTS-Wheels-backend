package com.tobeto.rentacar.services.dtos.requests.insurance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInsuranceRequests {

    private int id;

    private String policyNumber;

    private LocalDate startDate;

    private LocalDate endDate;

    private String details;
}
