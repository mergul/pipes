package com.streams.pipes.model.serdes;

import com.streams.pipes.model.BalanceCommand;
import org.springframework.kafka.support.serializer.JsonSerde;

public class BalanceCommandSerde extends JsonSerde<BalanceCommand> {
    public BalanceCommandSerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
