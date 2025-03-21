package br.com.yonlero.calculation.service.domain.enums;

public enum CalculationStatus {
    STARTED("STARTED"),
    ABORTED("ABORTED"),
    ERROR("ERROR"),
    FINISHED("FINISHED");

    private final String value;

    CalculationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
