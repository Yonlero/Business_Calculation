package br.com.yonlero.calculation.service.infrastructure.repository;

import br.com.yonlero.calculation.service.infrastructure.entity.BudgetOpeningJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetOpeningRepository extends JpaRepository<BudgetOpeningJPA, UUID> {
    List<BudgetOpeningJPA> findAll();

    @Query(value = "SELECT bo FROM BudgetOpeningJPA bo WHERE bo.yearMonth BETWEEN :startDate AND :endDate")
    List<BudgetOpeningJPA> findAllByYearMonthBetween(YearMonth startDate, YearMonth endDate);
}