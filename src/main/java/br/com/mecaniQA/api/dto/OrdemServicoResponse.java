package br.com.mecaniQA.api.dto;

import br.com.mecaniQA.api.model.StatusOrdemServico;
import java.time.LocalDateTime;
import java.util.List;

public record OrdemServicoResponse(Long codigo, StatusOrdemServico status, List<Long> codigosServicos,
                                   List<Long> codigosPecas,
                                   LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao) {
    public OrdemServicoResponse(Long codigo, StatusOrdemServico status, List<Long> codigosServicos,
                                LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao) {
        this(codigo, status, codigosServicos, List.of(), dataCriacao, dataUltimaAtualizacao);
    }

    public OrdemServicoResponse {
        codigosServicos = List.copyOf(codigosServicos);
        codigosPecas = List.copyOf(codigosPecas);
    }

}
