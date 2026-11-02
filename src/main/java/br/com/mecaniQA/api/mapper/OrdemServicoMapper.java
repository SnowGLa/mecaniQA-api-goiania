package br.com.mecaniQA.api.mapper;

import br.com.mecaniQA.api.dto.OrdemServicoRequest;
import br.com.mecaniQA.api.dto.OrdemServicoResponse;
import br.com.mecaniQA.api.model.OrdemServico;

public final class OrdemServicoMapper {
    private OrdemServicoMapper() { }

    public static OrdemServico toEntity(OrdemServicoRequest dto) {
        return OrdemServico.builder().codigosServicos(dto.codigosServicos())
                .codigosPecas(dto.codigosPecas()).build();
    }

    public static OrdemServicoResponse toResponse(OrdemServico ordem) {
        return new OrdemServicoResponse(ordem.getCodigo(), ordem.getStatus(), ordem.getCodigosServicos(),
                ordem.getCodigosPecas(), ordem.getDataCriacao(), ordem.getDataUltimaAtualizacao());
    }
}
