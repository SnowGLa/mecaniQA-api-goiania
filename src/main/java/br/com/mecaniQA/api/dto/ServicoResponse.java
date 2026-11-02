package br.com.mecaniQA.api.dto;

import java.time.LocalDateTime;

public record ServicoResponse(Long codigo, String nome, int tempoEstimadoMinutos, Double custoTabelado,
                              LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao) {
}
