package br.com.yonlero.calculation.service.interfaceadapter.dto.request;

import br.com.yonlero.calculation.service.domain.model.Calculation;

import java.time.LocalDate;
import java.util.UUID;

public record CalculationRequest(UUID id, LocalDate startDate, LocalDate endDate, UUID userId) {
    public Calculation toDomain() {
        return new Calculation(this.id, this.startDate, this.endDate, this.userId);
    }
}