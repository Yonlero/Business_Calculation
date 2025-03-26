package br.com.yonlero.apportionment.service.interfaceadapter.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ValueDistributionDTO(Integer month, BigDecimal percentage, UUID destinationId) {
}
