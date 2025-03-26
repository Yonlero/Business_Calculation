package br.com.yonlero.apportionment.service.application.usecase;

import br.com.yonlero.apportionment.service.domain.model.Apportionment;
import br.com.yonlero.apportionment.service.infrastructure.entity.ApportionmentJPA;
import br.com.yonlero.apportionment.service.infrastructure.repository.ApportionmentRepository;
import br.com.yonlero.apportionment.service.interfaceadapter.dto.request.NewApportionmentRequest;
import br.com.yonlero.apportionment.service.port.input.CreateApportionment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateApportionmentUseCase implements CreateApportionment {

    private final ApportionmentRepository apportionmentRepository;

    @Override
    public Apportionment newApportionment(NewApportionmentRequest apportionmentRequest) {
        Apportionment domain = apportionmentRequest.toDomain();

        ApportionmentJPA saved = apportionmentRepository.save(new ApportionmentJPA(domain));

        return saved.toDomain();
    }
}