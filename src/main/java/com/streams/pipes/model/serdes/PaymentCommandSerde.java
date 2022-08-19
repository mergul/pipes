package com.streams.pipes.model.serdes;

import com.streams.pipes.model.PaymentCommand;
import org.springframework.kafka.support.serializer.JsonSerde;

public class PaymentCommandSerde extends JsonSerde<PaymentCommand> {
    public PaymentCommandSerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
