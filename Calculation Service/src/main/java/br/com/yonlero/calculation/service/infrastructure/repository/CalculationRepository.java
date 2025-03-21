package br.com.yonlero.calculation.service.infrastructure.repository;

import br.com.yonlero.calculation.service.infrastructure.entity.CalculationJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CalculationRepository extends JpaRepository<CalculationJPA, UUID> {
}
