package br.com.yonlero.calculation.service.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public class BudgetOpening implements Serializable {
    private static final long serialVersionUID = -119063210143889619L;

    private UUID id;
    private YearMonth yearMonth;
    private String account;
    private String costCenter;
    private String businessUnit;
    private BigDecimal plannedValue;
    private BigDecimal projectedValue;
    private BigDecimal apportionmentValue;

    public BudgetOpening(UUID id, YearMonth yearMonth, String account, String costCenter, String businessUnit, BigDecimal plannedValue, BigDecimal projectedValue, BigDecimal apportionmentValue) {
        this.id = id;
        this.yearMonth = yearMonth;
        this.account = account;
        this.costCenter = costCenter;
        this.businessUnit = businessUnit;
        this.plannedValue = plannedValue;
        this.projectedValue = projectedValue;
        this.apportionmentValue = apportionmentValue;
    }

    public UUID getId() {
        return id;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
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

    public BigDecimal getPlannedValue() {
        return plannedValue;
    }

    public BigDecimal getProjectedValue() {
        return projectedValue;
    }

    public BigDecimal getApportionmentValue() {
        return apportionmentValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        BudgetOpening that = (BudgetOpening) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
