package br.com.yonlero.apportionment.service.port.input;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface KafkaConsumerPort {
    void consumerCalculateStartTopic(String calculation) throws JsonProcessingException;
}