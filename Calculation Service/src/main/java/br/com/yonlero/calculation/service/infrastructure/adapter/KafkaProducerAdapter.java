package br.com.yonlero.calculation.service.infrastructure.adapter;

import br.com.yonlero.calculation.service.interfaceadapter.dto.kafka.outgoing.CalculationKafka;
import br.com.yonlero.calculation.service.port.output.KafkaProducerPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProducerAdapter implements KafkaProducerPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void sendCalculationStartedEvent(String topic, CalculationKafka calc) {
        try {
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(calc));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}