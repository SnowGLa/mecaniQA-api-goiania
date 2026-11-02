package br.com.mecaniQA.api.mapper;

import br.com.mecaniQA.api.dto.PedidoPecasRequest;
import br.com.mecaniQA.api.dto.PedidoPecasResponse;
import br.com.mecaniQA.api.exception.DadosInvalidosException;
import br.com.mecaniQA.api.model.PedidoPecas;

public final class PedidoPecasMapper {
    private PedidoPecasMapper() {}

    public static PedidoPecas toEntity(PedidoPecasRequest dto) {
        if (dto.itens() == null) {
            throw new DadosInvalidosException("Informe a lista de itens, mesmo que vazia.");
        }
        return new PedidoPecas(dto.itens().stream().map(ItemPedidoPecaMapper::toEntity).toList());
    }

    public static PedidoPecasResponse toResponse(PedidoPecas pedido) {
        return new PedidoPecasResponse(
                pedido.getCodigo(),
                pedido.getStatus(),
                pedido.getItens().stream().map(ItemPedidoPecaMapper::toResponse).toList(),
                pedido.getDataCriacao(),
                pedido.getDataUltimaAtualizacao());
    }
}
