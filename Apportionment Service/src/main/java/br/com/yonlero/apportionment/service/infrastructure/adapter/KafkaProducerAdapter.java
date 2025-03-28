package br.com.yonlero.apportionment.service.infrastructure.adapter;

import br.com.yonlero.apportionment.service.interfaceadapter.dto.kafka.outgoing.ApportionmentOutgoing;
import br.com.yonlero.apportionment.service.port.output.KafkaProducerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProducerAdapter implements KafkaProducerPort {

    private final KafkaTemplate<String, ApportionmentOutgoing> kafkaTemplate;

    @Override
    public void sendApportionmentStatusProcess(String topic, ApportionmentOutgoing apportionment) {
        kafkaTemplate.send(topic, apportionment);
    }
}