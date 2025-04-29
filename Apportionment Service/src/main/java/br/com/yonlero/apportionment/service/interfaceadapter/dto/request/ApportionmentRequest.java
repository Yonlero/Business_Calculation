package br.com.yonlero.apportionment.service.interfaceadapter.dto.request;

import java.time.YearMonth;
import java.util.UUID;

public record ApportionmentRequest(UUID id, String account, String costCenter, String businessUnit, YearMonth yearMonth,
                                   UUID originId, boolean isOrigin) {
}
