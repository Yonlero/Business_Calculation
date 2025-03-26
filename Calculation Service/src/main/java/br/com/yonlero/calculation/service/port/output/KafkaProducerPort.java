package br.com.yonlero.calculation.service.port.output;

import br.com.yonlero.calculation.service.interfaceadapter.dto.kafka.outgoing.CalculationOutgoing;

public interface KafkaProducerPort {
    void sendCalculationStartedEvent(String topic, CalculationOutgoing calculation);
}
