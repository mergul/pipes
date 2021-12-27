package com.streams.pipes.config.processor;

import com.streams.pipes.chat.RoomEntre;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Configuration
public class ProcessorConf {
   @Bean(name = "sink")
   public <T> Sinks.Many<ServerSentEvent<T>> sink(){
       return Sinks.unsafe().many().replay().limit(2);
   }
    @Bean(name = "hotFlux")
    public <T> Flux<ServerSentEvent<T>> hotFlux(){
        Sinks.Many<ServerSentEvent<T>> sink = sink();
        return sink.asFlux();
    }
   @Bean(name = "roomEntre")
    public <T> RoomEntre<T> roomEntre(){
       Sinks.Many<ServerSentEvent<T>> sink = sink();
       Flux<ServerSentEvent<T>> hotFlux = hotFlux();
       return new RoomEntre<T>(hotFlux, sink);
   }
}
