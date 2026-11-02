package br.com.mecaniQA.api.dto;

import br.com.mecaniQA.api.model.StatusPedidoPecas;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoPecasResponse(Long codigo, StatusPedidoPecas status, List<ItemPedidoPecaResponse> itens,
                                  LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao) {
    public PedidoPecasResponse {
        itens = List.copyOf(itens);
    }

}
