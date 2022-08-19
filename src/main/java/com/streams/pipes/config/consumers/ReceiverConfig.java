package com.streams.pipes.config.consumers;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class ReceiverConfig {
  private static final Logger logger = LoggerFactory.getLogger(ReceiverConfig.class);

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "json");
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

    return props;
  }

  @Bean
  public ConsumerFactory<byte[], Object> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new ByteArrayDeserializer(),
        new JsonDeserializer<>().trustedPackages("*"));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<byte[], Object> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<byte[], Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setMessageConverter(new ByteArrayJsonMessageConverter());
    factory.setErrorHandler((thrownException, data) -> {
      String s = thrownException.getMessage().split("Error deserializing key/value for partition ")[1].split(". If needed, please seek past the record to continue consumption.")[0];
      String topics = s.split("-")[0];
      int offset = Integer.parseInt(s.split("offset ")[1]);
      int partition = Integer.parseInt(s.split("-")[1].split(" at")[0]);
      logger.info("Skipping " + topics + "-" + partition + " offset " + offset);
    });
    return factory;
  }

}
