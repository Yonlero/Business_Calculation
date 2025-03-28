package br.com.yonlero.apportionment.service.port.input;

public interface KafkaConsumerPort {
    void consumerCalculateStartTopic(String calculation);
}