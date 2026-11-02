package br.com.mecaniQA.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoPecasRequest(List<ItemPedidoPecaRequest> itens) {
    public PedidoPecasRequest {
        itens = itens == null ? null : Collections.unmodifiableList(new ArrayList<>(itens));
    }

}
