package br.com.yonlero.apportionment.service.port.input;

import br.com.yonlero.apportionment.service.domain.model.Apportionment;
import br.com.yonlero.apportionment.service.interfaceadapter.dto.request.NewApportionmentRequest;

public interface CreateApportionment {
    Apportionment newApportionment(NewApportionmentRequest apportionmentRequest);
}