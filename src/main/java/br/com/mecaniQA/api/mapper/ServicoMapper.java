package br.com.mecaniQA.api.mapper;

import br.com.mecaniQA.api.dto.ServicoRequest;
import br.com.mecaniQA.api.dto.ServicoResponse;
import br.com.mecaniQA.api.model.Servico;

public final class ServicoMapper {
    private ServicoMapper() { }

    public static Servico toEntity(ServicoRequest dto) {
        return new Servico(dto.nome(), dto.tempoEstimadoMinutos() == null ? 0 : dto.tempoEstimadoMinutos(),
                dto.custoTabelado());
    }

    public static void atualizar(Servico servico, ServicoRequest dto) {
        servico.setTempoEstimadoMinutos(dto.tempoEstimadoMinutos() == null ? 0 : dto.tempoEstimadoMinutos());
        servico.setCustoTabelado(dto.custoTabelado());
    }

    public static ServicoResponse toResponse(Servico servico) {
        return new ServicoResponse(servico.getCodigo(), servico.getNome(), servico.getTempoEstimadoMinutos(),
                servico.getCustoTabelado(), servico.getDataCriacao(), servico.getDataUltimaAtualizacao());
    }
}
