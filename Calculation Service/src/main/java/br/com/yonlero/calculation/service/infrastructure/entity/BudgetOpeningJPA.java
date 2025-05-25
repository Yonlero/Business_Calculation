package br.com.yonlero.calculation.service.infrastructure.entity;

import br.com.yonlero.calculation.service.domain.model.BudgetOpening;
import br.com.yonlero.calculation.service.infrastructure.converters.YearMonthIntegerConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "budget_opening")
public class BudgetOpeningJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Convert(converter = YearMonthIntegerConverter.class)
    private YearMonth yearMonth;

    @Column(nullable = false)
    private String account;

    @Column(nullable = false)
    private String costCenter;

    @Column(nullable = false)
    private String businessUnit;

    @Column(nullable = false)
    private BigDecimal plannedValue;

    @Column(nullable = false)
    private BigDecimal projectedValue;

    @Column(nullable = false)
    private BigDecimal apportionmentValue;

    public BudgetOpening toDomain() {
        return new BudgetOpening(this.id, this.yearMonth, this.account, this.costCenter, this.businessUnit, this.plannedValue, this.projectedValue, this.apportionmentValue);
    }

    public static BudgetOpeningJPA toJPA(BudgetOpening budgetOpening) {
        return new BudgetOpeningJPA(budgetOpening.getId(), budgetOpening.getYearMonth(), budgetOpening.getAccount(), budgetOpening.getCostCenter(),
                budgetOpening.getBusinessUnit(), budgetOpening.getPlannedValue(), budgetOpening.getProjectedValue(), budgetOpening.getApportionmentValue());
    }
}