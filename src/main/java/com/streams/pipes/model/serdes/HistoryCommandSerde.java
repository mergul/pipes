package com.streams.pipes.model.serdes;

import com.streams.pipes.model.HistoryCommand;
import org.springframework.kafka.support.serializer.JsonSerde;

public class HistoryCommandSerde extends JsonSerde<HistoryCommand> {
    public HistoryCommandSerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
