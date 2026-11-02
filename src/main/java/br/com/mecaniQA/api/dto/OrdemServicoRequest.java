package br.com.mecaniQA.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrdemServicoRequest(List<Long> codigosServicos, List<Long> codigosPecas) {
    public OrdemServicoRequest(List<Long> codigosServicos) {
        this(codigosServicos, List.of());
    }

    public OrdemServicoRequest {
        codigosServicos = codigosServicos == null ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(codigosServicos));
        codigosPecas = codigosPecas == null ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(codigosPecas));
    }

}
