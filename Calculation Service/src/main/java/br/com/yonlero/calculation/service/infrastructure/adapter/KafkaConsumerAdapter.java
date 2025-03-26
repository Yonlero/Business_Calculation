package br.com.yonlero.calculation.service.infrastructure.adapter;

import br.com.yonlero.calculation.service.domain.enums.KafkaTopics;
import br.com.yonlero.calculation.service.port.input.KafkaConsumerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumerAdapter implements KafkaConsumerPort {


    @Override
    @KafkaListener(
            topics = KafkaTopics.CALCULATION_APPORTIONMENT_RESPONSE,
            groupId = "calculation-group"
    )
    public void consumerApportionmentValues() {

    }
}