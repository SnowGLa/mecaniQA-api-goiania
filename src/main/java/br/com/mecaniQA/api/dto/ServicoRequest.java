package br.com.mecaniQA.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ServicoRequest(String nome, Integer tempoEstimadoMinutos, Double custoTabelado) {
}
