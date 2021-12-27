package com.streams.pipes.model.serdes;

import com.streams.pipes.model.RecordSSE;
import org.springframework.kafka.support.serializer.JsonSerde;

public class RecordSSESerde extends JsonSerde<RecordSSE> {
    public RecordSSESerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
