package br.com.yonlero.calculation.service.port.input;

import br.com.yonlero.calculation.service.interfaceadapter.dto.request.CalculationRequest;
import br.com.yonlero.calculation.service.interfaceadapter.dto.response.CalculationResponse;

public interface CalculateControllerPort {
    CalculationResponse startCalculateProcess(CalculationRequest request);
}
