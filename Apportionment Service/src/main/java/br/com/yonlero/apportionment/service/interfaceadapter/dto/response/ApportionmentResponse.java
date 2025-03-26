package br.com.yonlero.apportionment.service.interfaceadapter.dto.response;

import br.com.yonlero.apportionment.service.domain.model.Apportionment;
import br.com.yonlero.apportionment.service.domain.model.ValueDistribution;
import br.com.yonlero.apportionment.service.interfaceadapter.dto.ValueDistributionDTO;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ApportionmentResponse(UUID id, String account, String costCenter, String businessUnit, int year,
                                    UUID originId, boolean isOrigin, List<ValueDistributionDTO> values) {

    public static ApportionmentResponse fromDomain(Apportionment apportionment) {
        List<ValueDistributionDTO> values = new ArrayList<>();

        if (apportionment.getValuesByMonth() != null) {
            for (Map.Entry<Month, List<ValueDistribution>> entry : apportionment.getValuesByMonth().entrySet()) {
                List<ValueDistribution> distributions = entry.getValue();

                for (ValueDistribution distribution : distributions) {
                    BigDecimal value = distribution.getPercentage();
                    values.add(new ValueDistributionDTO(entry.getKey().getValue(), value, distribution.getDestinationId()));
                }
            }
        }

        return new ApportionmentResponse(
                apportionment.getId(),
                apportionment.getAccount(),
                apportionment.getCostCenter(),
                apportionment.getBusinessUnit(),
                apportionment.getYear(),
                apportionment.getOriginId(),
                apportionment.isOrigin(),
                values
        );
    }
}
