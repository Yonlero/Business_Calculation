package br.com.yonlero.apportionment.service.interfaceadapter.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class BudgetOpeningCacheDTO implements Serializable {
    private static final long serialVersionUID = -1368403368200496230L;

    private UUID id;
    private YearMonth yearMonth;
    private String account;
    private String costCenter;
    private String businessUnit;
    private BigDecimal plannedValue;
    private BigDecimal projectedValue;
    private BigDecimal apportionmentValue;
}
