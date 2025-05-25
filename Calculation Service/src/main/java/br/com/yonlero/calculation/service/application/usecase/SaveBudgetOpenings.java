package br.com.yonlero.calculation.service.application.usecase;

import br.com.yonlero.calculation.service.domain.model.BudgetOpening;
import br.com.yonlero.calculation.service.infrastructure.entity.BudgetOpeningJPA;
import br.com.yonlero.calculation.service.infrastructure.redis.RedisService;
import br.com.yonlero.calculation.service.infrastructure.repository.BudgetOpeningRepository;
import br.com.yonlero.calculation.service.port.input.SaveBudgetOpening;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaveBudgetOpenings implements SaveBudgetOpening {

    private final BudgetOpeningRepository repository;
    private final RedisService redisService;

    @Override
    public BudgetOpening saveBudgetOpening(BudgetOpening budgetOpeningToSave) {
        return null;
    }

    @Override
    public List<BudgetOpening> saveBudgetOpening(List<BudgetOpening> budgetOpeningsToSave) {
        return List.of();
    }

    @Override
    public void saveBudgetOpeningUpdatedInCacheByCalculation() {
        List<BudgetOpening> budgetOpeningInCache = redisService.getBudgetOpenings("#calculation.budget_opening");
        repository.saveAll(budgetOpeningInCache.stream().map(BudgetOpeningJPA::toJPA).collect(Collectors.toList()));
    }
}
