package com.streams.pipes.config.streams;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KAdminConf {

    @Value("${kafka.topics.sender-topics}")
    private String senderTopic;

    @Value("${kafka.topics.receiver-topics}")
    private String receiverTopic;

    @Value("${kafka.topics.auths}")
    private String authTopic;
    @Value("${kafka.topics.payments-in}")
    private String paymentsTopic;

    @Value("${kafka.topics.balances-in}")
    private String balancesTopic;


    @Value("${kafka.topics.listcom-in}")
    private String listcomTopics;
    @Value("${kafka.topics.partitioncom-in}")
    private String partitioncomTopics;
    @Value("${kafka.topics.paymentcom-in}")
    private String paymentcomTopics;
    @Value("${kafka.topics.balancecom-in}")
    private String balancecomTopics;
    @Value("${kafka.topics.checkout-in}")
    private String checkoutTopics;
    @Value("${kafka.topics.usersHistories-in}")
    private String usersHistoriesTopics;


    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic sendTopic() {
        return TopicBuilder.name(senderTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic countTopic() {
        return TopicBuilder.name(authTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic receiveTopic() {
        return TopicBuilder.name(receiverTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic paymentTopic() {
        return TopicBuilder.name(paymentsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic balancesTopic() {
        return TopicBuilder.name(balancesTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic listTopic() {
        return TopicBuilder.name(listcomTopics)
                .partitions(1)
                .replicas(1)
//                .compact()
                .build();
    }
    @Bean
    public NewTopic paymentComTopic() {
        return TopicBuilder.name(paymentcomTopics)
                .partitions(1)
                .replicas(1)
//                .compact()
                .build();
    }
    @Bean
    public NewTopic balanceComTopic() {
        return TopicBuilder.name(balancecomTopics)
                .partitions(1)
                .replicas(1)
//                .compact()
                .build();
    }
    @Bean
    public NewTopic partitionTopic() {
        return TopicBuilder.name(partitioncomTopics)
                .partitions(1)
                .replicas(1)
//                .compact()
                .build();
    }
    @Bean
    public NewTopic historyTopic() {
        return TopicBuilder.name(usersHistoriesTopics)
                .partitions(1)
                .replicas(1)
//                .compact()
                .build();
    }
    @Bean
    public NewTopic checkoutTopic() {
        return TopicBuilder.name(checkoutTopics)
                .partitions(1)
                .replicas(1)
//                .compact()
                .build();
    }
}
