package br.com.yonlero.apportionment.service.interfaceadapter.dto.kafka.incoming;

import java.time.LocalDate;
import java.util.UUID;

public record CalculationKafka(UUID id, LocalDate startDate, LocalDate endDate, UUID userId, String status,
                               UUID correlationId) {
}
