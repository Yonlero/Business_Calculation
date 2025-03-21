package br.com.yonlero.calculation.service.interfaceadapter.controller;

import br.com.yonlero.calculation.service.application.usecase.StartCalculation;
import br.com.yonlero.calculation.service.domain.model.Calculation;
import br.com.yonlero.calculation.service.interfaceadapter.dto.request.CalculationRequest;
import br.com.yonlero.calculation.service.interfaceadapter.dto.response.CalculationResponse;
import br.com.yonlero.calculation.service.port.input.CalculateControllerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calculations")
public class CalculationController implements CalculateControllerPort {

    private final StartCalculation startCalculationUseCase;

    @Override
    @PostMapping
    public CalculationResponse startCalculateProcess(@RequestBody CalculationRequest request) {
        Calculation calculation = startCalculationUseCase.startCalculation(
                request.startDate(),
                request.endDate(),
                request.userId()
        );

        return CalculationResponse.fromDomain(calculation);
    }
}