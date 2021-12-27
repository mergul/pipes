package com.streams.pipes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Date;
import java.util.Map;

@JsonDeserialize(builder = PaymentRecord.Builder.class)
public class PaymentRecord {

    private final String key;
    private final double payment;
    private final Long totalViews;
    private final Map<byte[], Long> ids;
    private final Date date;

    @JsonCreator(mode= JsonCreator.Mode.PROPERTIES)
    public PaymentRecord(String key, double payment, Long totalViews, Map<byte[], Long> ids, Date date) {
        this.key = key;
        this.payment = payment;
        this.totalViews = totalViews;
        this.ids = ids;
        this.date = date;
    }
    public String getKey() {
        return key;
    }

    public double getPayment() {
        return payment;
    }
    public Map<byte[], Long> getIds() {
        return ids;
    }

    public Long getTotalViews() {
        return totalViews;
    }
    public Date getDate() {
        return date;
    }


    public static Builder of(){
        return new Builder();
    }
    public static Builder from(PaymentRecord value2) {
        final Builder builder = new Builder();
        builder.key=value2.key;
        builder.payment=value2.payment;
        builder.totalViews=value2.totalViews;
        builder.ids=value2.ids;
        builder.date=value2.date;
        return builder;
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder {

        public String key;
        public double payment;
        public Long totalViews;
        public Date date;
        public Map<byte[], Long> ids;

        public Builder(){}

        public Builder(String key){
            this.key=key;
        }

        public Builder withKey(String key){
            this.key = key;
            return this;
        }

        public Builder withPayment(double payment){
            this.payment = payment;
            return this;
        }
        public Builder withTotalViews(Long totalViews){
            this.totalViews = totalViews;
            return this;
        }
        public Builder withIds(Map<byte[], Long> ids){
            this.ids = ids;
            return this;
        }
        public Builder withDate(Date date){
            this.date = date;
            return this;
        }
        public PaymentRecord build(){
            return new PaymentRecord(key, payment, totalViews, ids, date);
        }

    }
}
