package com.streams.pipes.model.serdes;

import com.streams.pipes.model.UserPayload;
import org.springframework.kafka.support.serializer.JsonSerde;

public class UserPayloadSerde extends JsonSerde<UserPayload> {
    public UserPayloadSerde(){
        super();
//        this.ignoreTypeHeaders();
    }
}
