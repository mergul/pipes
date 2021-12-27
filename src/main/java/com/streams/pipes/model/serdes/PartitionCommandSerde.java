package com.streams.pipes.model.serdes;

import com.streams.pipes.model.PartitionCommand;
import org.springframework.kafka.support.serializer.JsonSerde;

public class PartitionCommandSerde extends JsonSerde<PartitionCommand> {
    public PartitionCommandSerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
