package com.streams.pipes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafka
@EnableKafkaStreams
@ComponentScan(basePackages = {
		"com.streams.pipes.config",
		"com.streams.pipes.config.streams",
		"com.streams.pipes.config.web",
		"com.streams.pipes.config.producers",
		"com.streams.pipes.config.consumers",
		"com.streams.pipes.config.processor",
		"com.streams.pipes.model",
		"com.streams.pipes.chat",
		"com.streams.pipes.service",
		"com.streams.pipes.controller"
})
@SpringBootApplication
public class PipesApplication {

	public static void main(String[] args) {
		SpringApplication.run(PipesApplication.class, args);
	}

}
