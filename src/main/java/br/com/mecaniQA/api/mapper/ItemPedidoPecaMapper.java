package br.com.mecaniQA.api.mapper;

import br.com.mecaniQA.api.dto.ItemPedidoPecaRequest;
import br.com.mecaniQA.api.dto.ItemPedidoPecaResponse;
import br.com.mecaniQA.api.exception.DadosInvalidosException;
import br.com.mecaniQA.api.model.ItemPedidoPeca;

public final class ItemPedidoPecaMapper {
    private ItemPedidoPecaMapper() {}

    public static ItemPedidoPeca toEntity(ItemPedidoPecaRequest dto) {
        if (dto == null || dto.quantidade() == null) {
            throw new DadosInvalidosException("Informe um item e sua quantidade.");
        }
        return new ItemPedidoPeca(dto.codigoPeca(), dto.quantidade());
    }

    public static ItemPedidoPecaResponse toResponse(ItemPedidoPeca item) {
        return new ItemPedidoPecaResponse(item.getCodigoPeca(), item.getQuantidade());
    }
}
