package br.com.yonlero.calculation.service.port.output;

import br.com.yonlero.calculation.service.domain.model.Calculation;

public interface KafkaProducerPort {
    void sendCalculationStartedEvent(String topic, Calculation calculation);
}
