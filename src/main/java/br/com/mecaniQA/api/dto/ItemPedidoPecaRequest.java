package br.com.mecaniQA.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemPedidoPecaRequest(Long codigoPeca, Integer quantidade) {
}
