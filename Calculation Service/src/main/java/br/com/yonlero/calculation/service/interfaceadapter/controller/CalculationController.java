package br.com.yonlero.calculation.service.interfaceadapter.controller;

import br.com.yonlero.calculation.service.application.usecase.StartCalculation;
import br.com.yonlero.calculation.service.domain.model.Calculation;
import br.com.yonlero.calculation.service.interfaceadapter.dto.request.CalculationRequest;
import br.com.yonlero.calculation.service.interfaceadapter.dto.response.CalculationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calculations")
public class CalculationController {

    private final StartCalculation startCalculationUseCase;

    @PostMapping
    public CalculationResponse startCalculateProcess(@RequestBody CalculationRequest request) throws JsonProcessingException {
        Calculation calculation = startCalculationUseCase.startCalculation(
                request.startDate(),
                request.endDate(),
                request.userId()
        );

        return CalculationResponse.fromDomain(calculation);
    }
}