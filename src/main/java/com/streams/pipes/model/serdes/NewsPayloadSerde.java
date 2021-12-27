package com.streams.pipes.model.serdes;

import com.streams.pipes.model.NewsPayload;
import org.springframework.kafka.support.serializer.JsonSerde;

public class NewsPayloadSerde extends JsonSerde<NewsPayload> {
    public NewsPayloadSerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
