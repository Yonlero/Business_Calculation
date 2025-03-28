package br.com.yonlero.calculation.service.port.output;

import br.com.yonlero.calculation.service.interfaceadapter.dto.kafka.outgoing.CalculationKafka;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface KafkaProducerPort {
    void sendCalculationStartedEvent(String topic, CalculationKafka calculation) throws JsonProcessingException;
}
