package br.com.yonlero.apportionment.service.interfaceadapter.dto.request;

import br.com.yonlero.apportionment.service.domain.model.Apportionment;
import br.com.yonlero.apportionment.service.interfaceadapter.dto.ValueDistributionDTO;

import java.time.Month;
import java.util.List;
import java.util.UUID;

public record NewApportionmentRequest(UUID id, String account, String costCenter, String businessUnit, int year,
                                      UUID originId, boolean isOrigin, List<ValueDistributionDTO> valueDistribution) {

    public Apportionment toDomain() {
        Apportionment apportionment = new Apportionment(this.id, this.account, this.costCenter, this.businessUnit,
                this.originId, this.year, this.isOrigin);

        for (ValueDistributionDTO distribution : this.valueDistribution) {
            Month month = Month.of(distribution.month());

            apportionment.addNewValueDistribution(month, distribution.percentage(), distribution.destinationId());
        }


        return apportionment;
    }
}
