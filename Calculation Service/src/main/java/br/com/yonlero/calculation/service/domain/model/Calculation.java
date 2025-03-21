package br.com.yonlero.calculation.service.domain.model;

import br.com.yonlero.calculation.service.domain.enums.CalculationStatus;

import java.time.LocalDate;
import java.util.UUID;

public class Calculation {
    private UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID userId;
    private CalculationStatus status;

    public Calculation(UUID id, LocalDate startDate, LocalDate endDate, UUID userId) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
        this.status = CalculationStatus.STARTED;
    }

    public Calculation(LocalDate startDate, LocalDate endDate, UUID userId) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
        this.status = CalculationStatus.STARTED;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public UUID getUserId() {
        return userId;
    }

    public CalculationStatus getStatus() {
        return status;
    }

    public void setStatus(CalculationStatus status) {
        this.status = status;
    }
}