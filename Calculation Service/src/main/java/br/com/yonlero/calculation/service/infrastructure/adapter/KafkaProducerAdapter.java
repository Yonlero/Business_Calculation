package br.com.yonlero.calculation.service.infrastructure.adapter;

import br.com.yonlero.calculation.service.interfaceadapter.dto.kafka.outgoing.CalculationOutgoing;
import br.com.yonlero.calculation.service.port.output.KafkaProducerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProducerAdapter implements KafkaProducerPort {

    private final KafkaTemplate<String, CalculationOutgoing> kafkaTemplate;

    @Override
    public void sendCalculationStartedEvent(String topic, CalculationOutgoing calc) {
        kafkaTemplate.send(topic, calc);
    }
}