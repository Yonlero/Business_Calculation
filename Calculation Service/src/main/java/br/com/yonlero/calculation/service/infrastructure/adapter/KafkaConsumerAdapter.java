package br.com.yonlero.calculation.service.infrastructure.adapter;

import br.com.yonlero.calculation.service.domain.model.KafkaTopics;
import br.com.yonlero.calculation.service.port.input.KafkaConsumerPort;
import br.com.yonlero.calculation.service.port.input.SaveBudgetOpening;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumerAdapter implements KafkaConsumerPort {

    private final SaveBudgetOpening saveBudgetOpening;

    @Override
    @KafkaListener(
            topics = KafkaTopics.APPORTIONMENT_CALCULATION_FINISHED,
            groupId = "calculation-group"
    )
    public void consumerBudgetOpeningsCalculatedByApportionment() {
        saveBudgetOpening.saveBudgetOpeningUpdatedInCacheByCalculation();
    }
}