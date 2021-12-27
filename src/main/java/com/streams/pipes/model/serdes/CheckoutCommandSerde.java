package com.streams.pipes.model.serdes;

import com.streams.pipes.model.CheckoutCommand;
import org.springframework.kafka.support.serializer.JsonSerde;

public class CheckoutCommandSerde extends JsonSerde<CheckoutCommand> {
    public CheckoutCommandSerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
