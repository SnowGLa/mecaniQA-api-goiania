package br.com.mecaniQA.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

class Oat2HttpTests {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final br.com.mecaniQA.api.support.TestRepositories banco =
            new br.com.mecaniQA.api.support.TestRepositories();
    private final MockMvc mvc = banco.mvc();
    private long servico;
    private long primeiraPeca;
    private long segundaPeca;

    @BeforeEach
    void criarReferenciasViaDtos() throws Exception {
        servico =
                resposta(
                                post("/api/servicos")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"nome\":\"Revisao\",\"tempoEstimadoMinutos\":60,\"custoTabelado\":100.0}"),
                                201)
                        .get("codigo")
                        .longValue();
        primeiraPeca = criarPeca();
        segundaPeca = criarPeca();
    }

    @AfterEach
    void limparReferencias() throws Exception {
        mvc.perform(delete("/api/servicos/" + servico));
        mvc.perform(delete("/api/pecas/" + primeiraPeca));
        mvc.perform(delete("/api/pecas/" + segundaPeca));
    }

    @Test
    void cadastraListaEBuscaOrdemProtegendoCamposGerados() throws Exception {
        var ordem = criarOrdem();
        assertEquals(mapper.valueToTree("ABERTO"), ordem.get("status"));
        verificarCamposGerados(ordem);
        assertEquals(servico, ordem.get("codigosServicos").get(0).longValue());
        assertEquals(ordem, resposta(get("/api/ordens-servico/" + codigo(ordem)), 200));
        var lista = resposta(get("/api/ordens-servico"), 200);
        assertTrue(lista.isArray());
        assertTrue(lista.valueStream().anyMatch(ordem::equals));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"codigosServicos\":null}",
                "{\"codigosServicos\":[]}",
                "{\"codigosServicos\":[null]}",
                "{\"codigosServicos\":[0]}",
                "{\"codigosServicos\":[-1]}",
                "{\"codigosServicos\":[9223372036854775807]}",
                "{\"codigosServicos\":\"invalido\"}",
                "{"
            })
    void rejeitaOrdemInvalidaSemCadastrar(String json) throws Exception {
        var antes = resposta(get("/api/ordens-servico"), 200);
        resposta(
                post("/api/ordens-servico").contentType(MediaType.APPLICATION_JSON).content(json),
                400);
        assertEquals(antes, resposta(get("/api/ordens-servico"), 200));
    }

    @Test
    void rejeitaServicosRepetidosNaOrdem() throws Exception {
        resposta(
                post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigosServicos\":[" + servico + "," + servico + "]}"),
                400);
    }

    @Test
    void ordemSegueTodasAsEtapasSemRetrocederPularOuRepetir() throws Exception {
        var ordem = criarOrdem();
        String rota = "/api/ordens-servico/" + codigo(ordem);
        resposta(
                put(rota + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAGO\"}"),
                400);
        assertEquals(ordem, resposta(get(rota), 200));
        for (String status :
                new String[] {"PENDENTE_PAGAMENTO", "PAGO", "EM_EXECUCAO", "EXECUTADO"}) {
            var atualizado =
                    resposta(
                            put(rota + "/status")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"status\":\""
                                                    + status
                                                    + "\",\"codigo\":-1,"
                                                    + "\"dataCriacao\":\"1900-01-01T00:00:00\","
                                                    + "\"dataUltimaAtualizacao\":\"1900-01-01T00:00:00\"}"),
                            200);
            assertEquals(mapper.valueToTree(status), atualizado.get("status"));
            assertEquals(ordem.get("codigo"), atualizado.get("codigo"));
            assertEquals(ordem.get("dataCriacao"), atualizado.get("dataCriacao"));
            assertEquals(ordem.get("codigosServicos"), atualizado.get("codigosServicos"));
            verificarCamposGerados(atualizado);
            resposta(
                    put(rota + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"" + status + "\"}"),
                    400);
            resposta(
                    put(rota + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"ABERTO\"}"),
                    400);
            assertEquals(atualizado, resposta(get(rota), 200));
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"status\":null}",
                "{\"status\":\"INEXISTENTE\"}",
                "{\"status\":42}"
            })
    void rejeitaStatusDesconhecidoOuAusente(String json) throws Exception {
        var ordem = criarOrdem();
        var pedido = criarPedidoVazio();
        resposta(
                put("/api/ordens-servico/" + codigo(ordem) + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json),
                400);
        resposta(
                put("/api/pedidos-pecas/" + codigo(pedido) + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json),
                400);
        assertEquals(ordem, resposta(get("/api/ordens-servico/" + codigo(ordem)), 200));
        assertEquals(pedido, resposta(get("/api/pedidos-pecas/" + codigo(pedido)), 200));
    }

    @Test
    void rotasRetornam404ParaCodigosInexistentes() throws Exception {
        resposta(get("/api/ordens-servico/-1"), 404);
        resposta(get("/api/pedidos-pecas/-1"), 404);
        resposta(
                put("/api/ordens-servico/-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PENDENTE_PAGAMENTO\"}"),
                404);
        resposta(
                put("/api/pedidos-pecas/-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PENDENTE_PAGAMENTO\"}"),
                404);
        resposta(
                post("/api/pedidos-pecas/-1/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(item(primeiraPeca, 1)),
                404);
    }

    @Test
    void cadastraPedidoComVariasPecasEConsolidaDuplicadas() throws Exception {
        var pedido =
                resposta(
                        post("/api/pedidos-pecas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"itens\":["
                                                + item(primeiraPeca, 2)
                                                + ","
                                                + item(segundaPeca, 4)
                                                + ","
                                                + item(primeiraPeca, 3)
                                                + "],\"codigo\":-1,\"status\":\"ENTREGUE\","
                                                + "\"dataCriacao\":\"1900-01-01T00:00:00\","
                                                + "\"dataUltimaAtualizacao\":\"1900-01-01T00:00:00\"}"),
                        201);
        assertEquals(mapper.valueToTree("ORCANDO"), pedido.get("status"));
        verificarCamposGerados(pedido);
        assertEquals(2, pedido.get("itens").size());
        assertEquals(5, quantidade(pedido, primeiraPeca));
        assertEquals(4, quantidade(pedido, segundaPeca));
        assertEquals(pedido, resposta(get("/api/pedidos-pecas/" + codigo(pedido)), 200));
        assertTrue(resposta(get("/api/pedidos-pecas"), 200).valueStream().anyMatch(pedido::equals));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"itens\":null}",
                "{\"itens\":[null]}",
                "{\"itens\":[{\"codigoPeca\":null,\"quantidade\":1}]}",
                "{\"itens\":[{\"codigoPeca\":9223372036854775807,\"quantidade\":1}]}",
                "{"
            })
    void rejeitaCadastroDePedidoInvalidoSemCadastrar(String json) throws Exception {
        var antes = resposta(get("/api/pedidos-pecas"), 200);
        resposta(
                post("/api/pedidos-pecas").contentType(MediaType.APPLICATION_JSON).content(json),
                400);
        assertEquals(antes, resposta(get("/api/pedidos-pecas"), 200));
    }

    @Test
    void adicionaVariasPecasESomaQuantidadeDaMesmaPeca() throws Exception {
        var pedido = criarPedidoVazio();
        assertEquals(0, pedido.get("itens").size());
        String rota = "/api/pedidos-pecas/" + codigo(pedido);
        adicionarItem(rota, primeiraPeca, 2);
        adicionarItem(rota, segundaPeca, 4);
        var atualizado = adicionarItem(rota, primeiraPeca, 3);
        assertEquals(2, atualizado.get("itens").size());
        assertEquals(5, quantidade(atualizado, primeiraPeca));
        assertEquals(4, quantidade(atualizado, segundaPeca));
        assertEquals(pedido.get("codigo"), atualizado.get("codigo"));
        assertEquals(pedido.get("dataCriacao"), atualizado.get("dataCriacao"));
        assertEquals(atualizado, resposta(get(rota), 200));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "null"})
    void rejeitaQuantidadeInvalidaNaInclusaoENoCadastro(String quantidade) throws Exception {
        var pedido = criarPedidoVazio();
        String json = "{\"codigoPeca\":" + primeiraPeca + ",\"quantidade\":" + quantidade + "}";
        resposta(
                post("/api/pedidos-pecas/" + codigo(pedido) + "/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json),
                400);
        resposta(
                post("/api/pedidos-pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itens\":[" + json + "]}"),
                400);
        assertEquals(pedido, resposta(get("/api/pedidos-pecas/" + codigo(pedido)), 200));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"codigoPeca\":null,\"quantidade\":1}",
                "{\"codigoPeca\":0,\"quantidade\":1}",
                "{\"codigoPeca\":-1,\"quantidade\":1}"
            })
    void rejeitaItemIncompletoOuCodigoInvalido(String json) throws Exception {
        var pedido = criarPedidoVazio();
        resposta(
                post("/api/pedidos-pecas/" + codigo(pedido) + "/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json),
                400);
        assertEquals(pedido, resposta(get("/api/pedidos-pecas/" + codigo(pedido)), 200));
    }

    @Test
    void pecaInexistenteRetorna404SemModificarPedido() throws Exception {
        var pedido = criarPedidoVazio();
        resposta(
                post("/api/pedidos-pecas/" + codigo(pedido) + "/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(item(Long.MAX_VALUE, 1)),
                404);
        assertEquals(pedido, resposta(get("/api/pedidos-pecas/" + codigo(pedido)), 200));
    }

    @Test
    void overflowDeQuantidadeRetorna400SemModificarPedido() throws Exception {
        String rota = "/api/pedidos-pecas/" + codigo(criarPedidoVazio());
        var antes = adicionarItem(rota, primeiraPeca, Integer.MAX_VALUE);
        resposta(
                post(rota + "/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(item(primeiraPeca, 1)),
                400);
        assertEquals(antes, resposta(get(rota), 200));
    }

    @Test
    void pedidoSegueTodasAsEtapasSemRetrocederPularOuRepetir() throws Exception {
        var pedido = criarPedidoVazio();
        String rota = "/api/pedidos-pecas/" + codigo(pedido);
        resposta(
                put(rota + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ENTREGUE\"}"),
                400);
        assertEquals(pedido, resposta(get(rota), 200));
        for (String status : new String[] {"PENDENTE_PAGAMENTO", "PAGO_FATURADO", "ENTREGUE"}) {
            var atualizado =
                    resposta(
                            put(rota + "/status")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"status\":\""
                                                    + status
                                                    + "\",\"codigo\":-1,"
                                                    + "\"dataCriacao\":\"1900-01-01T00:00:00\","
                                                    + "\"dataUltimaAtualizacao\":\"1900-01-01T00:00:00\"}"),
                            200);
            assertEquals(mapper.valueToTree(status), atualizado.get("status"));
            assertEquals(pedido.get("codigo"), atualizado.get("codigo"));
            assertEquals(pedido.get("dataCriacao"), atualizado.get("dataCriacao"));
            verificarCamposGerados(atualizado);
            resposta(
                    put(rota + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"" + status + "\"}"),
                    400);
            resposta(
                    put(rota + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"ORCANDO\"}"),
                    400);
            assertEquals(atualizado, resposta(get(rota), 200));
        }
    }

    @Test
    void endpointsExistentesPreservamCrudComDtosECamposGeradosProtegidos() throws Exception {
        var servicoAntes = resposta(get("/api/servicos/" + servico), 200);
        var servicoDepois =
                resposta(
                        put("/api/servicos/" + servico)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"codigo\":-1,\"tempoEstimadoMinutos\":90,\"custoTabelado\":150.0,"
                                            + "\"dataCriacao\":\"1900-01-01T00:00:00\","
                                            + "\"dataUltimaAtualizacao\":\"1900-01-01T00:00:00\"}"),
                        200);
        assertEquals(servicoAntes.get("codigo"), servicoDepois.get("codigo"));
        assertEquals(servicoAntes.get("dataCriacao"), servicoDepois.get("dataCriacao"));
        assertEquals(90, servicoDepois.get("tempoEstimadoMinutos").intValue());
        var pecaAntes = resposta(get("/api/pecas/" + primeiraPeca), 200);
        var pecaDepois =
                resposta(
                        put("/api/pecas/" + primeiraPeca)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"codigo\":-1,\"quantidadeEstoque\":8,\"precoCusto\":12.0,\"precoVenda\":25.0,"
                                            + "\"dataCadastro\":\"1900-01-01T00:00:00\","
                                            + "\"dataUltimaAtualizacao\":\"1900-01-01T00:00:00\"}"),
                        200);
        assertEquals(pecaAntes.get("codigo"), pecaDepois.get("codigo"));
        assertEquals(pecaAntes.get("dataCadastro"), pecaDepois.get("dataCadastro"));
        assertEquals(8, pecaDepois.get("quantidadeEstoque").intValue());
        resposta(get("/api/pecas"), 200);
        resposta(get("/api/servicos"), 200);
        resposta(
                post("/api/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"nome\":\"Sem"
                                    + " categoria\",\"codigoBarras\":\"123\",\"fornecedorMarca\":\"Marca\",\"quantidadeEstoque\":0,\"precoCusto\":0,\"precoVenda\":0}"),
                400);
        mvc.perform(delete("/api/servicos/" + servico)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/pecas/" + primeiraPeca)).andExpect(status().isNoContent());
        resposta(get("/api/servicos/" + servico), 404);
        resposta(get("/api/pecas/" + primeiraPeca), 404);
    }

    private long criarPeca() throws Exception {
        return resposta(
                        post("/api/pecas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"nome\":\"Filtro\",\"categoria\":\"MOTOR\",\"codigoBarras\":\"123\",\"fornecedorMarca\":\"Marca\",\"quantidadeEstoque\":0,\"precoCusto\":0,\"precoVenda\":0,\"codigo\":-1,"
                                            + "\"dataCadastro\":\"1900-01-01T00:00:00\","
                                            + "\"dataUltimaAtualizacao\":\"1900-01-01T00:00:00\"}"),
                        201)
                .get("codigo")
                .longValue();
    }

    private JsonNode criarOrdem() throws Exception {
        return resposta(
                post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"codigosServicos\":["
                                        + servico
                                        + "],\"codigo\":-1,\"status\":\"EXECUTADO\","
                                        + "\"dataCriacao\":\"1900-01-01T00:00:00\","
                                        + "\"dataUltimaAtualizacao\":\"1900-01-01T00:00:00\"}"),
                201);
    }

    private JsonNode criarPedidoVazio() throws Exception {
        return resposta(
                post("/api/pedidos-pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"itens\":[],\"codigo\":-1,\"status\":\"ENTREGUE\","
                                        + "\"dataCriacao\":\"1900-01-01T00:00:00\","
                                        + "\"dataUltimaAtualizacao\":\"1900-01-01T00:00:00\"}"),
                201);
    }

    private JsonNode adicionarItem(String rota, long peca, int quantidade) throws Exception {
        return resposta(
                post(rota + "/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(item(peca, quantidade)),
                200);
    }

    private String item(long peca, int quantidade) {
        return mapper.writeValueAsString(Map.of("codigoPeca", peca, "quantidade", quantidade));
    }

    private int quantidade(JsonNode pedido, long codigoPeca) {
        return pedido.get("itens")
                .valueStream()
                .filter(i -> i.get("codigoPeca").longValue() == codigoPeca)
                .findFirst()
                .orElseThrow()
                .get("quantidade")
                .intValue();
    }

    private long codigo(JsonNode entidade) {
        return entidade.get("codigo").longValue();
    }

    private void verificarCamposGerados(JsonNode entidade) {
        assertTrue(codigo(entidade) > 0);
        assertNotNull(entidade.get("dataCriacao"));
        assertFalse(entidade.get("dataCriacao").isNull());
        assertNotEquals(mapper.valueToTree("1900-01-01T00:00:00"), entidade.get("dataCriacao"));
        assertNotNull(entidade.get("dataUltimaAtualizacao"));
        assertFalse(entidade.get("dataUltimaAtualizacao").isNull());
        assertNotEquals(
                mapper.valueToTree("1900-01-01T00:00:00"), entidade.get("dataUltimaAtualizacao"));
    }

    private JsonNode resposta(MockHttpServletRequestBuilder request, int esperado)
            throws Exception {
        var resultado = mvc.perform(request).andExpect(status().is(esperado)).andReturn();
        String corpo = resultado.getResponse().getContentAsString();
        return corpo.isBlank() ? null : mapper.readTree(corpo);
    }
}
