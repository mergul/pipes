package com.streams.pipes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class Sender {

  private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

  private final KafkaTemplate<byte[], Object> kafkaTemplate;

  public Sender(KafkaTemplate<byte[], Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public Mono<Boolean> send(String topic, Object payload, byte[] key, Boolean isstream) {
    LOGGER.info("sending payload='{}' to topic='{}'", payload, topic);
    if (isstream) {
      kafkaTemplate.send(topic, key, payload);
    } else kafkaTemplate.send(MessageBuilder.withPayload(payload).setHeader(KafkaHeaders.TOPIC, topic)
            .build());
    return Mono.just(true);
  }
}
// .setHeader("__TypeId__", clazz.getName())
