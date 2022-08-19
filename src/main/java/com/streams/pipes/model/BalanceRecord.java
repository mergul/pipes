package com.streams.pipes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Date;

//@Document(collection = "balances")
@JsonDeserialize(builder = BalanceRecord.Builder.class)
public class BalanceRecord {

    private String key;
    private String paymentKey;
    private double payment;
    private Long pageviews;
    private Long totalViews;
    private Long payedViews;
    private double totalBalance;
    private boolean mustPay;
    private Date date;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BalanceRecord(String key, String paymentKey, Long pageviews, double payment, Long totalViews, Long payedViews, double totalBalance, boolean mustPay, Date date) {
        this.key = key;
        this.paymentKey = paymentKey;
        this.pageviews = pageviews;
        this.payment = payment;
        this.totalViews = totalViews;
        this.payedViews = payedViews;
        this.totalBalance = totalBalance;
        this.mustPay = mustPay;
        this.date = date;
    }

    public BalanceRecord() { }

    public String getKey() {
        return key;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getPageviews() {
        return pageviews;
    }

    public double getPayment() {
        return payment;
    }

    public Long getTotalViews() {
        return totalViews;
    }

    public Long getPayedViews() {
        return payedViews;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public boolean getMustPay() {
        return mustPay;
    }

    public Date getDate() {
        return date;
    }

    public static Builder of() {
        return new Builder();
    }

    public static Builder from(BalanceRecord value2) {
        final Builder builder = new Builder();
        builder.key = value2.key;
        builder.paymentKey = value2.paymentKey;
        builder.pageviews = value2.pageviews;
        builder.payment = value2.payment;
        builder.totalViews = value2.totalViews;
        builder.payedViews = value2.payedViews;
        builder.totalBalance = value2.totalBalance;
        builder.mustPay = value2.mustPay;
        builder.date = value2.date;
        return builder;
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder {
        public String paymentKey;
        public String key;
        public Long pageviews;
        public double payment;
        public Long totalViews;
        public Long payedViews;
        public double totalBalance;
        public boolean mustPay;
        public Date date;


        public Builder() {
        }

        public Builder(String key) {
            this.key = key;
        }

        public Builder withKey(String key) {
            this.key = key;
            return this;
        }

        public Builder withPaymentKey(String paymentKey) {
            this.paymentKey = paymentKey;
            return this;
        }

        public Builder withPageviews(Long pageviews) {
            this.pageviews = pageviews;
            return this;
        }

        public Builder withDate(Date date) {
            this.date = date;
            return this;
        }

        public Builder withPayment(double payment) {
            this.payment = payment;
            return this;
        }

        public Builder withTotalBalance(double totalBalance) {
            this.totalBalance = totalBalance;
            return this;
        }

        public Builder withTotalViews(Long totalViews) {
            this.totalViews = totalViews;
            return this;
        }

        public Builder withPayedViews(Long payedViews) {
            this.payedViews = payedViews;
            return this;
        }
        public Builder withMustPay(boolean mustPay) {
            this.mustPay = mustPay;
            return this;
        }

        public BalanceRecord build() {
            return new BalanceRecord(key, paymentKey, pageviews, payment, totalViews, payedViews, totalBalance, mustPay, date);
        }
    }
}
