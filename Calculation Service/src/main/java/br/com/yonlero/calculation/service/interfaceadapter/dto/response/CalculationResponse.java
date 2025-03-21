package br.com.yonlero.calculation.service.interfaceadapter.dto.response;

import br.com.yonlero.calculation.service.domain.model.Calculation;

import java.time.LocalDate;
import java.util.UUID;

public record CalculationResponse(UUID id, LocalDate startDate, LocalDate endDate, UUID userId, String status) {

    public static CalculationResponse fromDomain(Calculation calculation) {
        return new CalculationResponse(
                calculation.getId(),
                calculation.getStartDate(),
                calculation.getEndDate(),
                calculation.getUserId(),
                calculation.getStatus().getValue()
        );
    }
}