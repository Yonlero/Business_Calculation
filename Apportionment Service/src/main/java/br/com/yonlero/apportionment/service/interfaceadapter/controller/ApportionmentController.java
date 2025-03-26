package br.com.yonlero.apportionment.service.interfaceadapter.controller;

import br.com.yonlero.apportionment.service.domain.model.Apportionment;
import br.com.yonlero.apportionment.service.interfaceadapter.dto.request.NewApportionmentRequest;
import br.com.yonlero.apportionment.service.interfaceadapter.dto.response.ApportionmentResponse;
import br.com.yonlero.apportionment.service.port.input.CreateApportionment;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/apportionments")
public class ApportionmentController {

    private final CreateApportionment createApportionment;

    @PostMapping
    public ApportionmentResponse createNewApportionment(@RequestBody NewApportionmentRequest request) {
        Apportionment apportionment = createApportionment.newApportionment(request);

        return ApportionmentResponse.fromDomain(apportionment);
    }

}