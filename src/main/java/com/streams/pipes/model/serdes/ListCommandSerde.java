package com.streams.pipes.model.serdes;

import com.streams.pipes.model.ListCommand;
import org.springframework.kafka.support.serializer.JsonSerde;

public class ListCommandSerde extends JsonSerde<ListCommand> {
    public ListCommandSerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
