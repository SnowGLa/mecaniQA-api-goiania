package br.com.mecaniQA.api;

import br.com.mecaniQA.api.controller.OrdemServicoController;
import br.com.mecaniQA.api.controller.PecaController;
import br.com.mecaniQA.api.controller.PedidoPecasController;
import br.com.mecaniQA.api.controller.ServicoController;
import br.com.mecaniQA.api.dto.*;
import br.com.mecaniQA.api.model.StatusOrdemServico;
import br.com.mecaniQA.api.model.StatusPedidoPecas;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoContractTests {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void dtosDeEntradaNaoPossuemCodigosDatasOuStatusGerados() {
        String camposGerados = "\"codigo\":-1,\"dataCadastro\":\"1900-01-01T00:00:00\","
                + "\"dataCriacao\":\"1900-01-01T00:00:00\","
                + "\"dataUltimaAtualizacao\":\"1900-01-01T00:00:00\",\"status\":\"PAGO\"";
        var entradas = List.of(
                mapper.readValue("{" + camposGerados + ",\"categoria\":\"MOTOR\"}", PecaRequest.class),
                mapper.readValue("{" + camposGerados + ",\"nome\":\"Revisao\"}", ServicoRequest.class),
                mapper.readValue("{" + camposGerados + ",\"codigosServicos\":[1]}", OrdemServicoRequest.class),
                mapper.readValue("{" + camposGerados + ",\"itens\":[]}", PedidoPecasRequest.class));
        for (var entrada : entradas) {
            var json = mapper.valueToTree(entrada);
            for (String campo : List.of("codigo", "dataCadastro", "dataCriacao", "dataUltimaAtualizacao", "status")) {
                assertFalse(json.has(campo), entrada.getClass().getSimpleName() + ": " + campo);
            }
        }
    }

    @Test
    void listasDosDtosDeEntradaSaoCopiasImutaveis() {
        var codigos = new ArrayList<>(List.of(1L));
        var ordem = new OrdemServicoRequest(codigos);
        codigos.clear();
        assertEquals(List.of(1L), ordem.codigosServicos());
        assertThrows(UnsupportedOperationException.class, () -> ordem.codigosServicos().clear());
        var itens = new ArrayList<>(List.of(new ItemPedidoPecaRequest(1L, 2)));
        var pedido = new PedidoPecasRequest(itens);
        itens.clear();
        assertEquals(1, pedido.itens().size());
        assertThrows(UnsupportedOperationException.class, () -> pedido.itens().clear());
    }

    @Test
    void listasDosDtosDeRespostaSaoCopiasImutaveis() {
        var agora = LocalDateTime.now();
        var codigos = new ArrayList<>(List.of(1L));
        var ordem = new OrdemServicoResponse(1L, StatusOrdemServico.ABERTO, codigos, agora, agora);
        codigos.clear();
        assertEquals(List.of(1L), ordem.codigosServicos());
        assertThrows(UnsupportedOperationException.class, () -> ordem.codigosServicos().clear());
        var itens = new ArrayList<>(List.of(new ItemPedidoPecaResponse(1L, 2)));
        var pedido = new PedidoPecasResponse(1L, StatusPedidoPecas.ORCANDO, itens, agora, agora);
        itens.clear();
        assertEquals(1, pedido.itens().size());
        assertThrows(UnsupportedOperationException.class, () -> pedido.itens().clear());
    }

    @Test
    void controllersNaoExpoemEntidadesNosContratosHttp() {
        for (var tipo : List.of(PecaController.class, ServicoController.class,
                OrdemServicoController.class, PedidoPecasController.class)) {
            Arrays.stream(tipo.getDeclaredMethods()).filter(m -> !m.isSynthetic()).forEach(metodo -> {
                assertFalse(metodo.getGenericReturnType().getTypeName().contains(".model."), metodo.toString());
                Arrays.stream(metodo.getParameters()).filter(p -> p.isAnnotationPresent(RequestBody.class))
                        .forEach(p -> assertEquals("br.com.mecaniQA.api.dto", p.getType().getPackageName()));
            });
        }
    }
}
