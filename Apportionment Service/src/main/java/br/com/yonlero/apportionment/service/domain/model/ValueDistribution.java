package br.com.yonlero.apportionment.service.domain.model;

import java.math.BigDecimal;
import java.time.Month;
import java.util.UUID;

public class ValueDistribution {
    private Month month;
    private BigDecimal percentage;
    private UUID destinationId;

    public ValueDistribution(Month month, BigDecimal percentage, UUID destinationId) {
        this.month = month;
        this.percentage = percentage;
        this.destinationId = destinationId;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public UUID getDestinationId() {
        return destinationId;
    }
}
