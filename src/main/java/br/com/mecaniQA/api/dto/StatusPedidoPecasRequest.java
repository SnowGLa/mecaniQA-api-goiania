package br.com.mecaniQA.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatusPedidoPecasRequest(String status) {
}
