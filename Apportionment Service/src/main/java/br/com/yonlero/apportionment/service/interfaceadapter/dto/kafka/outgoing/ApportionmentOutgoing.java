package br.com.yonlero.apportionment.service.interfaceadapter.dto.kafka.outgoing;

import br.com.yonlero.apportionment.service.interfaceadapter.dto.ValueDistributionDTO;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public record ApportionmentOutgoing(UUID apportionmentId, UUID correlationId, String account, String costCenter,
                                    String businessUnit, YearMonth yearMonth, UUID originId, boolean isOrigin,
                                    List<ValueDistributionDTO> values) {
}
