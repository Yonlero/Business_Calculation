package br.com.yonlero.apportionment.service.interfaceadapter.dto.request;

import java.util.UUID;

public record ApportionmentRequest(UUID id, String account, String costCenter, String businessUnit, int year,
                                   UUID originId, boolean isOrigin) {
}
