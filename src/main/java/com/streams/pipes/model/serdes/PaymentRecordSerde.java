package com.streams.pipes.model.serdes;

import com.streams.pipes.model.PaymentRecord;
import org.springframework.kafka.support.serializer.JsonSerde;

public class PaymentRecordSerde extends JsonSerde<PaymentRecord> {
    public PaymentRecordSerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
