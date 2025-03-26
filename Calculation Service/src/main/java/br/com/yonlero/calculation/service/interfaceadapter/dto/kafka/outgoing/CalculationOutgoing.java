package br.com.yonlero.calculation.service.interfaceadapter.dto.kafka.outgoing;

import br.com.yonlero.calculation.service.domain.model.Calculation;

import java.time.LocalDate;
import java.util.UUID;

public record CalculationOutgoing(UUID id, LocalDate startDate, LocalDate endDate, UUID userId, String status,
                                  UUID correlationId) {

    public static CalculationOutgoing fromDomain(Calculation calculation) {
        return new CalculationOutgoing(
                calculation.getId(),
                calculation.getStartDate(),
                calculation.getEndDate(),
                calculation.getUserId(),
                calculation.getStatus().getValue(),
                UUID.randomUUID()
        );
    }
}
