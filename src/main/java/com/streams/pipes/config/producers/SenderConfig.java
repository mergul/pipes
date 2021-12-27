package com.streams.pipes.config.producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SenderConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    return props;
  }

  @Bean
  public ProducerFactory<byte[], Object> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs(),
            new ByteArraySerializer(), new JsonSerializer<>(new ObjectMapper()));
  }

  @Bean
  public KafkaTemplate<byte[],Object> kafkaTemplate() {
    KafkaTemplate<byte[], Object> template = new KafkaTemplate<>(producerFactory());
    template.setMessageConverter(new ByteArrayJsonMessageConverter());

    return template;
  }

//  @Bean
//  public Map<String, Object> producerConfigs() {
//    Map<String, Object> props = new HashMap<>();
//    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
//    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//
//    return props;
//  }
//
//  @Bean
//  public ProducerFactory<byte[], Object> producerFactory() {
//    return new DefaultKafkaProducerFactory<>(producerConfigs(),
//            new ByteArraySerializer(), new JsonSerializer<>(new ObjectMapper()));
//  }
//
//  @Bean
//  public KafkaTemplate<byte[],Object> kafkaTemplate() {
//    KafkaTemplate<byte[], Object> template = new KafkaTemplate<>(producerFactory());
//    template.setMessageConverter(new ByteArrayJsonMessageConverter());
//
//    return template;
//  }

}
