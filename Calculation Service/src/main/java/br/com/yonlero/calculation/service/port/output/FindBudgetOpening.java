package br.com.yonlero.calculation.service.port.output;

import br.com.yonlero.calculation.service.domain.model.BudgetOpening;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface FindBudgetOpening {
    // Return Collections
    List<BudgetOpening> findAllBudgetOpening();
    List<BudgetOpening> findBudgetOpeningToCalculate(YearMonth startDate, YearMonth endDate);

    // Return Single Object
    BudgetOpening findBudgetOpeningById(UUID id);

}