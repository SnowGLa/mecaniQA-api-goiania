package br.com.mecaniQA.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.mecaniQA.api.controller.OrdemServicoController;
import br.com.mecaniQA.api.controller.PecaController;
import br.com.mecaniQA.api.controller.PedidoPecasController;
import br.com.mecaniQA.api.controller.ServicoController;
import br.com.mecaniQA.api.dto.OrdemServicoRequest;
import br.com.mecaniQA.api.dto.OrdemServicoResponse;
import br.com.mecaniQA.api.mapper.OrdemServicoMapper;
import br.com.mecaniQA.api.model.OrdemServico;
import br.com.mecaniQA.api.model.Peca;
import br.com.mecaniQA.api.model.Servico;
import br.com.mecaniQA.api.model.StatusOrdemServico;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Oat2Revisao2Tests {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final br.com.mecaniQA.api.support.TestRepositories banco =
            new br.com.mecaniQA.api.support.TestRepositories();
    private final MockMvc mvc = banco.mvc();
    private Peca peca;
    private Servico servico;

    @BeforeEach
    void prepararReferencias() {
        peca = banco.pecas.save(new Peca());
        servico = banco.servicos.save(new Servico());
    }

    @AfterEach
    void limparReferencias() {
        banco.pecas.deleteById(peca.getCodigo());
        banco.servicos.deleteById(servico.getCodigo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"servicos", "pecas", "ambos"})
    void apiCriaOrdensComServicosPecasOuAmbosSempreAbertas(String tipo) throws Exception {
        String listas =
                switch (tipo) {
                    case "servicos" -> "\"codigosServicos\":[" + servico.getCodigo() + "]";
                    case "pecas" -> "\"codigosPecas\":[" + peca.getCodigo() + "]";
                    default ->
                            "\"codigosServicos\":["
                                    + servico.getCodigo()
                                    + "],\"codigosPecas\":["
                                    + peca.getCodigo()
                                    + "]";
                };
        String json =
                "{"
                        + listas
                        + ",\"status\":\"EXECUTADO\",\"codigo\":-1,"
                        + "\"dataCriacao\":\"1900-01-01T00:00:00\",\"dataUltimaAtualizacao\":\"1900-01-01T00:00:00\"}";
        JsonNode resposta =
                mapper.readTree(
                        mvc.perform(
                                        post("/api/ordens-servico")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(json))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString());
        assertEquals(mapper.valueToTree("ABERTO"), resposta.get("status"));
        assertTrue(resposta.get("codigo").longValue() > 0);
        assertNotEquals(mapper.valueToTree("1900-01-01T00:00:00"), resposta.get("dataCriacao"));
        assertNotEquals(
                mapper.valueToTree("1900-01-01T00:00:00"), resposta.get("dataUltimaAtualizacao"));
        assertEquals(
                tipo.equals("pecas") ? List.of() : List.of(servico.getCodigo()),
                resposta.get("codigosServicos").valueStream().map(JsonNode::longValue).toList());
        assertEquals(
                tipo.equals("servicos") ? List.of() : List.of(peca.getCodigo()),
                resposta.get("codigosPecas").valueStream().map(JsonNode::longValue).toList());
        mvc.perform(get("/api/ordens-servico/" + resposta.get("codigo").longValue()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"codigosServicos\":[],\"codigosPecas\":[]}",
                "{\"codigosPecas\":[null]}",
                "{\"codigosPecas\":[0]}",
                "{\"codigosPecas\":[-1]}",
                "{\"codigosPecas\":[9223372036854775807]}"
            })
    void apiRejeitaOrdensVaziasOuPecasInvalidas(String json) throws Exception {
        mvc.perform(
                        post("/api/ordens-servico")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void apiRejeitaPecasRepetidasMesmoComServicosValidos() throws Exception {
        mvc.perform(
                        post("/api/ordens-servico")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"codigosServicos\":["
                                                + servico.getCodigo()
                                                + "],\"codigosPecas\":["
                                                + peca.getCodigo()
                                                + ","
                                                + peca.getCodigo()
                                                + "]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void builderPadraoPermiteSomentePecasEUsaAberto() {
        var ordem = OrdemServico.builder().codigosPecas(List.of(peca.getCodigo())).build();
        assertEquals(StatusOrdemServico.ABERTO, ordem.getStatus());
        assertTrue(ordem.getCodigosServicos().isEmpty());
        assertEquals(List.of(peca.getCodigo()), ordem.getCodigosPecas());
    }

    @ParameterizedTest
    @EnumSource(StatusOrdemServico.class)
    void builderAceitaStatusInternoExplicito(StatusOrdemServico status) {
        var ordem =
                OrdemServico.builder()
                        .status(status)
                        .codigosPecas(List.of(peca.getCodigo()))
                        .build();
        assertEquals(status, ordem.getStatus());
    }

    @Test
    void builderRejeitaStatusNuloEReferenciasInvalidas() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        OrdemServico.builder()
                                .status(null)
                                .codigosPecas(List.of(peca.getCodigo()))
                                .build());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        OrdemServico.builder()
                                .codigosServicos(List.of(servico.getCodigo()))
                                .codigosPecas(null)
                                .build());
        List<List<Long>> invalidas =
                List.of(
                        Arrays.asList((Long) null),
                        List.of(0L),
                        List.of(-1L),
                        List.of(Long.MAX_VALUE),
                        List.of(peca.getCodigo(), peca.getCodigo()));
        for (var codigos : invalidas) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> banco.ordemService.criar(new OrdemServicoRequest(List.of(), codigos)));
        }
    }

    @Test
    void pecasDoBuilderDaEntidadeEDosDtosSaoCopiasImutaveis() throws Exception {
        var original = new ArrayList<>(List.of(peca.getCodigo()));
        var builder = OrdemServico.builder().codigosPecas(original);
        original.clear();
        var campo = OrdemServico.Builder.class.getDeclaredField("codigosPecas");
        campo.setAccessible(true);
        assertThrows(
                UnsupportedOperationException.class, () -> ((List<?>) campo.get(builder)).clear());
        var ordem = builder.build();
        builder.codigosPecas(List.of());
        assertEquals(List.of(peca.getCodigo()), ordem.getCodigosPecas());
        assertThrows(UnsupportedOperationException.class, () -> ordem.getCodigosPecas().clear());

        original.add(peca.getCodigo());
        var entrada = new OrdemServicoRequest(List.of(), original);
        var resposta =
                new OrdemServicoResponse(
                        ordem.getCodigo(),
                        ordem.getStatus(),
                        List.of(),
                        original,
                        ordem.getDataCriacao(),
                        ordem.getDataUltimaAtualizacao());
        original.clear();
        assertEquals(List.of(peca.getCodigo()), entrada.codigosPecas());
        assertEquals(List.of(peca.getCodigo()), resposta.codigosPecas());
        assertThrows(UnsupportedOperationException.class, () -> entrada.codigosPecas().clear());
        assertThrows(UnsupportedOperationException.class, () -> resposta.codigosPecas().clear());
        assertEquals(
                entrada.codigosPecas(), OrdemServicoMapper.toEntity(entrada).getCodigosPecas());
        assertEquals(ordem.getCodigosPecas(), OrdemServicoMapper.toResponse(ordem).codigosPecas());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "POST /api/pecas",
                "PUT /api/pecas/ID_PECA",
                "POST /api/servicos",
                "PUT /api/servicos/ID_SERVICO",
                "POST /api/ordens-servico",
                "PUT /api/ordens-servico/-1/status",
                "POST /api/pedidos-pecas",
                "POST /api/pedidos-pecas/-1/itens",
                "PUT /api/pedidos-pecas/-1/status"
            })
    void corpoJsonLiteralNullRetorna400(String endpoint) throws Exception {
        String[] partes =
                endpoint.replace("ID_PECA", peca.getCodigo().toString())
                        .replace("ID_SERVICO", servico.getCodigo().toString())
                        .split(" ", 2);
        var requisicao = partes[0].equals("POST") ? post(partes[1]) : put(partes[1]);
        mvc.perform(requisicao.contentType(MediaType.APPLICATION_JSON).content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void controllersTambemRejeitamDtoNuloAntesDeConsultarRecursos() {
        assertEquals(
                400, new PecaController(banco.pecaService).criar(null).getStatusCode().value());
        assertEquals(
                400,
                new PecaController(banco.pecaService).atualizar(-1L, null).getStatusCode().value());
        assertEquals(
                400,
                new ServicoController(banco.servicoService).criar(null).getStatusCode().value());
        assertEquals(
                400,
                new ServicoController(banco.servicoService)
                        .atualizar(-1L, null)
                        .getStatusCode()
                        .value());
        assertEquals(
                400,
                new OrdemServicoController(banco.ordemService).criar(null).getStatusCode().value());
        assertEquals(
                400,
                new OrdemServicoController(banco.ordemService)
                        .alterarStatus(-1L, null)
                        .getStatusCode()
                        .value());
        assertEquals(
                400,
                new PedidoPecasController(banco.pedidoService).criar(null).getStatusCode().value());
        assertEquals(
                400,
                new PedidoPecasController(banco.pedidoService)
                        .adicionarItem(-1L, null)
                        .getStatusCode()
                        .value());
        assertEquals(
                400,
                new PedidoPecasController(banco.pedidoService)
                        .alterarStatus(-1L, null)
                        .getStatusCode()
                        .value());
    }
}
