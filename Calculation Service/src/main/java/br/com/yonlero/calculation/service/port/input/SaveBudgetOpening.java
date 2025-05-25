package br.com.yonlero.calculation.service.port.input;

import br.com.yonlero.calculation.service.domain.model.BudgetOpening;

import java.util.List;

public interface SaveBudgetOpening {

    BudgetOpening saveBudgetOpening(BudgetOpening budgetOpeningToSave);
    List<BudgetOpening> saveBudgetOpening(List<BudgetOpening> budgetOpeningsToSave);

    void saveBudgetOpeningUpdatedInCacheByCalculation();
}