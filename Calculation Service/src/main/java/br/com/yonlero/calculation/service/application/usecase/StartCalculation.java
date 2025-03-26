package br.com.yonlero.calculation.service.application.usecase;

import br.com.yonlero.calculation.service.domain.model.KafkaTopics;
import br.com.yonlero.calculation.service.domain.model.Calculation;
import br.com.yonlero.calculation.service.infrastructure.entity.CalculationJPA;
import br.com.yonlero.calculation.service.infrastructure.repository.CalculationRepository;
import br.com.yonlero.calculation.service.interfaceadapter.dto.kafka.outgoing.CalculationOutgoing;
import br.com.yonlero.calculation.service.port.output.KafkaProducerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartCalculation {
    private final CalculationRepository calculationRepository;
    private final KafkaProducerPort kafkaProducerPort;

    public Calculation startCalculation(LocalDate startDate, LocalDate endDate, UUID userId) {
        Calculation calculation = new Calculation(startDate, endDate, userId);

        CalculationJPA savedCalculationJPA = calculationRepository.save(new CalculationJPA(calculation));
        kafkaProducerPort.sendCalculationStartedEvent(KafkaTopics.CALCULATION_STARTED, CalculationOutgoing.fromDomain(savedCalculationJPA.toDomain()));

        return savedCalculationJPA.toDomain();
    }

}