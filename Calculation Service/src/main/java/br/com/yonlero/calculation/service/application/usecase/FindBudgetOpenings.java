package br.com.yonlero.calculation.service.application.usecase;

import br.com.yonlero.calculation.service.domain.model.BudgetOpening;
import br.com.yonlero.calculation.service.infrastructure.entity.BudgetOpeningJPA;
import br.com.yonlero.calculation.service.infrastructure.redis.RedisService;
import br.com.yonlero.calculation.service.infrastructure.repository.BudgetOpeningRepository;
import br.com.yonlero.calculation.service.port.output.FindBudgetOpening;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindBudgetOpenings implements FindBudgetOpening {

    private final BudgetOpeningRepository repository;
    private final RedisService redisService;

    @Override
    public List<BudgetOpening> findAllBudgetOpening() {
        return List.of();
    }

    @Override
    public List<BudgetOpening> findBudgetOpeningToCalculate(YearMonth startDate, YearMonth endDate) {

        List<BudgetOpening> budgetOpenings = new ArrayList<>(repository.findAllByYearMonthBetween(startDate, endDate).stream().map(BudgetOpeningJPA::toDomain).toList());

        redisService.put("#calculation.budget_opening", (Serializable) budgetOpenings);
        return budgetOpenings;
    }

    @Override
    public BudgetOpening findBudgetOpeningById(UUID id) {
        return null;
    }
}