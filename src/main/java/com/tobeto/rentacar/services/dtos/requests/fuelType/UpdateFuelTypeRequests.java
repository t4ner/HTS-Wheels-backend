package com.tobeto.rentacar.services.dtos.requests.fuelType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFuelTypeRequests {

    private int id;

    private String type;

}
