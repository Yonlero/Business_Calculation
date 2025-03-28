package br.com.yonlero.apportionment.service.infrastructure.adapter;

import br.com.yonlero.apportionment.service.application.usecase.ApportionmentProcessor;
import br.com.yonlero.apportionment.service.domain.model.Apportionment;
import br.com.yonlero.apportionment.service.domain.model.KafkaTopics;
import br.com.yonlero.apportionment.service.infrastructure.entity.ApportionmentJPA;
import br.com.yonlero.apportionment.service.infrastructure.repository.ApportionmentRepository;
import br.com.yonlero.apportionment.service.port.input.KafkaConsumerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KafkaConsumerAdapter implements KafkaConsumerPort {

    private final ApportionmentProcessor processor;
    private final ApportionmentRepository repository;

    @Override
    @KafkaListener(
            topics = KafkaTopics.CALCULATION_STARTED,
            groupId = "apportionment-group"
    )
    public void consumerCalculateStartTopic(String calculation) {
        List<ApportionmentJPA> apportionmentJPAS = repository.findAll();
        List<Apportionment> apportionments = apportionmentJPAS.stream().map(ApportionmentJPA::toDomain).toList();

        processor.clearDataStructures();
        apportionments.parallelStream().forEach(processor::addApportionment);

        processor.processAll();
    }
}