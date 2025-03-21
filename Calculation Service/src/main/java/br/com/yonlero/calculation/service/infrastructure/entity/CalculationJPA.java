package br.com.yonlero.calculation.service.infrastructure.entity;

import br.com.yonlero.calculation.service.domain.enums.CalculationStatus;
import br.com.yonlero.calculation.service.domain.model.Calculation;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Table(name = "calculations")
public class CalculationJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalculationStatus status;

    protected CalculationJPA() {
    }

    public CalculationJPA(Calculation calculation) {
        this.id = calculation.getId();
        this.startDate = calculation.getStartDate();
        this.endDate = calculation.getEndDate();
        this.userId = calculation.getUserId();
        this.status = calculation.getStatus();
    }

    public Calculation toDomain() {
        Calculation calculation = new Calculation(this.id, this.startDate, this.endDate, this.userId);
        calculation.setStatus(this.status);
        return calculation;
    }
}