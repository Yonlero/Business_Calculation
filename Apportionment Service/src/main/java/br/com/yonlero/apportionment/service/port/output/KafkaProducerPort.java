package br.com.yonlero.apportionment.service.port.output;

import br.com.yonlero.apportionment.service.interfaceadapter.dto.kafka.outgoing.ApportionmentOutgoing;

public interface KafkaProducerPort {
    void sendApportionmentStatusProcess(String topic, ApportionmentOutgoing apportionment);
}