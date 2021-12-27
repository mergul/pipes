package com.streams.pipes.model.serdes;

import com.streams.pipes.model.BalanceRecord;
import org.springframework.kafka.support.serializer.JsonSerde;

public class BalanceRecordSerde extends JsonSerde<BalanceRecord> {
    public BalanceRecordSerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
