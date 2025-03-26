package br.com.yonlero.apportionment.service.domain.model;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Apportionment {
    private UUID id;
    private String account;
    private String costCenter;
    private String businessUnit;
    private int year;
    private UUID originId;
    private boolean isOrigin;

    private Map<Month, List<ValueDistribution>> valuesByMonth = new HashMap<>();

    private Map<Month, BigDecimal> totalPercentagesByMonth = new HashMap<>();

    public Apportionment(UUID id, String account, String costCenter, String businessUnit, UUID originId, int year, boolean isOrigin) {
        this.id = id;
        this.account = account;
        this.costCenter = costCenter;
        this.businessUnit = businessUnit;
        this.year = year;
        this.originId = originId;
        this.isOrigin = isOrigin;
    }

    public void addNewValueDistribution(Month month, BigDecimal percentage, UUID destinationId) {
        ValueDistribution distribution = new ValueDistribution(month, percentage, destinationId);

        valuesByMonth.computeIfAbsent(month, k -> new ArrayList<>()).add(distribution);

        totalPercentagesByMonth.merge(month, percentage, BigDecimal::add);

        if (totalPercentagesByMonth.get(month).compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("The percentage in this month " + month + " is greater than 100%.");
        }
    }

    public List<ValueDistribution> getDistributionsForMonth(Month month) {
        return valuesByMonth.getOrDefault(month, Collections.emptyList());
    }

    public void validateAllPercentages() {
        totalPercentagesByMonth.forEach((month, totalPercentage) -> {
            if (totalPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("The sum of the percentages for the month " + month + " is greater than 100%.");
            }
        });
    }

    public UUID getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public int getYear() {
        return year;
    }

    public UUID getOriginId() {
        return originId;
    }

    public boolean isOrigin() {
        return isOrigin;
    }

    public Map<Month, List<ValueDistribution>> getValuesByMonth() {
        return valuesByMonth;
    }

    public void setValuesByMonth(Map<Month, List<ValueDistribution>> valuesByMonth) {
        this.valuesByMonth = valuesByMonth;
    }
}
