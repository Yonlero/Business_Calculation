package br.com.yonlero.apportionment.service.infrastructure.adapter;

import br.com.yonlero.apportionment.service.application.usecase.ApportionmentProcessor;
import br.com.yonlero.apportionment.service.domain.model.KafkaTopics;
import br.com.yonlero.apportionment.service.infrastructure.repository.ApportionmentRepository;
import br.com.yonlero.apportionment.service.interfaceadapter.dto.kafka.incoming.CalculationKafka;
import br.com.yonlero.apportionment.service.port.input.KafkaConsumerPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumerAdapter implements KafkaConsumerPort {

    private final ApportionmentProcessor processor;
    private final ApportionmentRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @KafkaListener(
            topics = KafkaTopics.CALCULATION_STARTED,
            groupId = "apportionment-group"
    )
    public void consumerCalculateStartTopic(String calculation) throws JsonProcessingException {
        CalculationKafka calculationKafka = objectMapper.readValue(calculation, CalculationKafka.class);

        processor.clearDataStructures();
        processor.processAllToCalculation(calculationKafka);
    }
}