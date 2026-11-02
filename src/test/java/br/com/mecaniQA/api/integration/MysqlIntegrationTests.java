package br.com.mecaniQA.api.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.mecaniQA.api.controller.PecaController;
import br.com.mecaniQA.api.dto.ItemPedidoPecaRequest;
import br.com.mecaniQA.api.dto.StatusOrdemServicoRequest;
import br.com.mecaniQA.api.exception.ConflitoDadosException;
import br.com.mecaniQA.api.exception.DadosInvalidosException;
import br.com.mecaniQA.api.exception.PersistenciaException;
import br.com.mecaniQA.api.jdbc.JdbcConnectionFactory;
import br.com.mecaniQA.api.model.CategoriaPeca;
import br.com.mecaniQA.api.model.ItemPedidoPeca;
import br.com.mecaniQA.api.model.OrdemServico;
import br.com.mecaniQA.api.model.Peca;
import br.com.mecaniQA.api.model.PedidoPecas;
import br.com.mecaniQA.api.model.Servico;
import br.com.mecaniQA.api.model.StatusOrdemServico;
import br.com.mecaniQA.api.repository.OrdemServicoRepository;
import br.com.mecaniQA.api.repository.PecaRepository;
import br.com.mecaniQA.api.repository.PedidoPecasRepository;
import br.com.mecaniQA.api.repository.ServicoRepository;
import br.com.mecaniQA.api.service.OrdemServicoService;
import br.com.mecaniQA.api.service.PecaService;
import br.com.mecaniQA.api.service.PedidoPecasService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import tools.jackson.databind.json.JsonMapper;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** MySQL real, schema previamente criado. Nao inicia banco nem executa DDL. */
@Tag("mysql")
@EnabledIfEnvironmentVariable(named = "MYSQL_TEST_URL", matches = "jdbc:mysql:.*")
@EnabledIfEnvironmentVariable(named = "MYSQL_TEST_USERNAME", matches = ".+")
@EnabledIfEnvironmentVariable(named = "MYSQL_TEST_PASSWORD", matches = ".*")
class MysqlIntegrationTests {
    private final JdbcConnectionFactory factory =
            new JdbcConnectionFactory(
                    System.getenv("MYSQL_TEST_URL"),
                    System.getenv("MYSQL_TEST_USERNAME"),
                    System.getenv("MYSQL_TEST_PASSWORD"));
    private final PecaRepository pecas = new PecaRepository(factory);
    private final ServicoRepository servicos = new ServicoRepository(factory);
    private final OrdemServicoRepository ordens = new OrdemServicoRepository(factory);
    private final PedidoPecasRepository pedidos = new PedidoPecasRepository(factory);
    private final List<Long> pecaIds = new ArrayList<>(),
            servicoIds = new ArrayList<>(),
            ordemIds = new ArrayList<>(),
            pedidoIds = new ArrayList<>();

    private Peca peca() {
        var p =
                pecas.save(
                        new Peca(
                                "Teste integracao",
                                "OAT3",
                                "Teste",
                                CategoriaPeca.MOTOR,
                                10.25,
                                20.50));
        pecaIds.add(p.getCodigo());
        return p;
    }

    private Servico servico() {
        var s = servicos.save(new Servico("Teste integracao", 30, 99.99));
        servicoIds.add(s.getCodigo());
        return s;
    }

    private OrdemServico ordem() {
        var o =
                ordens.save(
                        OrdemServico.builder()
                                .codigosServicos(List.of(servico().getCodigo()))
                                .codigosPecas(List.of(peca().getCodigo()))
                                .build());
        ordemIds.add(o.getCodigo());
        return o;
    }

    private PedidoPecas pedido() {
        var p = pedidos.save(new PedidoPecas(List.of(new ItemPedidoPeca(peca().getCodigo(), 2))));
        pedidoIds.add(p.getCodigo());
        return p;
    }

    @AfterEach
    void limparSomenteRegistrosCriados() {
        ordemIds.forEach(ordens::deleteById);
        pedidoIds.forEach(pedidos::deleteById);
        servicoIds.forEach(servicos::deleteById);
        pecaIds.forEach(pecas::deleteById);
    }

    static Stream<Arguments> camposInvalidosDoCatalogo() {
        Stream<Arguments> nulos =
                Stream.of(
                                "peca:nome",
                                "peca:codigo_barras",
                                "peca:fornecedor_marca",
                                "peca:categoria",
                                "peca:quantidade_estoque",
                                "peca:preco_custo",
                                "peca:preco_venda",
                                "servico:nome",
                                "servico:tempo_estimado_minutos",
                                "servico:custo_tabelado")
                        .map(campo -> Arguments.of(campo, true));
        Stream<Arguments> negativos =
                Stream.of(
                                "peca:quantidade_estoque",
                                "peca:preco_custo",
                                "peca:preco_venda",
                                "servico:tempo_estimado_minutos",
                                "servico:custo_tabelado")
                        .map(campo -> Arguments.of(campo, false));
        return Stream.concat(nulos, negativos);
    }

    @ParameterizedTest
    @MethodSource("camposInvalidosDoCatalogo")
    void bancoRejeitaObrigatoriosNulosENumerosNegativos(String alvo, boolean nulo)
            throws Exception {
        String[] partes = alvo.split(":");
        String tabela = partes[0];
        Map<String, Object> valores = new LinkedHashMap<>();
        valores.put("nome", "Teste constraint");
        if (tabela.equals("peca")) {
            valores.put("codigo_barras", "OAT3");
            valores.put("fornecedor_marca", "Teste");
            valores.put("categoria", "MOTOR");
            valores.put("quantidade_estoque", 0);
            valores.put("preco_custo", 0);
            valores.put("preco_venda", 0);
            valores.put("data_cadastro", Timestamp.valueOf(LocalDateTime.now()));
        } else {
            valores.put("tempo_estimado_minutos", 0);
            valores.put("custo_tabelado", 0);
            valores.put("data_criacao", Timestamp.valueOf(LocalDateTime.now()));
        }
        valores.put("data_ultima_atualizacao", Timestamp.valueOf(LocalDateTime.now()));
        valores.put(partes[1], nulo ? null : -1);
        String sql =
                "INSERT INTO "
                        + tabela
                        + " ("
                        + String.join(",", valores.keySet())
                        + ") VALUES ("
                        + String.join(",", java.util.Collections.nCopies(valores.size(), "?"))
                        + ")";
        try (var conexao = factory.abrir()) {
            conexao.setAutoCommit(false);
            try (var statement = conexao.prepareStatement(sql)) {
                int indice = 1;
                for (Object valor : valores.values()) {
                    statement.setObject(indice++, valor);
                }
                var erro = assertThrows(SQLException.class, statement::executeUpdate);
                assertEquals(nulo ? 1048 : 3819, erro.getErrorCode());
            } finally {
                conexao.rollback();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"peca", "servico"})
    void bancoAceitaZerosEOpcionaisNulos(String tabela) {
        if (tabela.equals("peca")) {
            var peca =
                    pecas.save(
                            new Peca("Teste zero", "OAT3", "Teste", CategoriaPeca.MOTOR, 0.0, 0.0));
            pecaIds.add(peca.getCodigo());
            assertEquals(0, peca.getQuantidadeEstoque());
            assertEquals(0.0, peca.getPrecoCusto());
            assertEquals(0.0, peca.getPrecoVenda());
            assertNull(peca.getTamanho());
            assertNull(peca.getCor());
        } else {
            var servico = servicos.save(new Servico("Teste zero", 0, 0.0));
            servicoIds.add(servico.getCodigo());
            assertEquals(0, servico.getTempoEstimadoMinutos());
            assertEquals(0.0, servico.getCustoTabelado());
        }
    }

    @Test
    void conectaComDuasConexoesReaisIndependentes() throws Exception {
        try (var a = factory.abrir();
                var b = factory.abrir()) {
            assertNotSame(a, b);
            assertTrue(a.isValid(5));
            assertTrue(b.isValid(5));
        }
    }

    @Test
    void credenciaisIncorretasProduzemExcecaoSanitizada() {
        var invalida =
                new JdbcConnectionFactory(
                        System.getenv("MYSQL_TEST_URL"),
                        "oat3_usuario_inexistente",
                        "senha_de_teste_invalida");
        var erro = assertThrows(PersistenciaException.class, invalida::abrir);
        assertEquals("Nao foi possivel conectar ao banco de dados.", erro.getMessage());
        assertNotNull(erro.getCause());
    }

    @Test
    void crudPecaComValoresDecimaisEDatas() {
        var p = peca();
        assertNotNull(p.getCodigo());
        assertEquals(10.25, p.getPrecoCusto());
        assertTrue(pecas.findAll().stream().anyMatch(v -> v.getCodigo().equals(p.getCodigo())));
        p.setPrecoVenda(30.75);
        var atual = pecas.update(p);
        assertEquals(30.75, atual.getPrecoVenda());
        assertEquals(p.getDataCadastro(), atual.getDataCadastro());
        assertTrue(pecas.deleteById(p.getCodigo()));
        assertTrue(pecas.findById(p.getCodigo()).isEmpty());
        assertFalse(pecas.deleteById(p.getCodigo()));
    }

    @Test
    void crudServicoComAtualizacaoEExclusao() {
        var s = servico();
        s.setTempoEstimadoMinutos(90);
        var atual = servicos.update(s);
        assertEquals(
                90, servicos.findById(atual.getCodigo()).orElseThrow().getTempoEstimadoMinutos());
        assertTrue(servicos.findAll().stream().anyMatch(v -> v.getCodigo().equals(s.getCodigo())));
        assertTrue(servicos.deleteById(s.getCodigo()));
        assertTrue(servicos.findById(s.getCodigo()).isEmpty());
    }

    @Test
    void crudOrdemPreservaListasStatusEDatasEmOutraInstancia() {
        var o = ordem();
        var lida = new OrdemServicoRepository(factory).findById(o.getCodigo()).orElseThrow();
        assertEquals(o.getCodigosPecas(), lida.getCodigosPecas());
        assertEquals(o.getCodigosServicos(), lida.getCodigosServicos());
        var service = new OrdemServicoService(ordens, pecas, servicos);
        var atual =
                service.alterarStatus(
                        o.getCodigo(), new StatusOrdemServicoRequest("PENDENTE_PAGAMENTO"));
        assertEquals(o.getDataCriacao(), atual.getDataCriacao());
        assertEquals(StatusOrdemServico.PENDENTE_PAGAMENTO, atual.getStatus());
        assertTrue(ordens.findAll().stream().anyMatch(v -> v.getCodigo().equals(o.getCodigo())));
        assertTrue(ordens.deleteById(o.getCodigo()));
        assertTrue(ordens.findById(o.getCodigo()).isEmpty());
    }

    @Test
    void crudPedidoPersisteMultiplasPecasSomaEOverflowSemAlterar() {
        var p = pedido();
        var outra = peca();
        var svc = new PedidoPecasService(pedidos, pecas);
        svc.adicionarItem(p.getCodigo(), new ItemPedidoPecaRequest(outra.getCodigo(), 3));
        var atual =
                svc.adicionarItem(
                        p.getCodigo(),
                        new ItemPedidoPecaRequest(p.getItens().getFirst().getCodigoPeca(), 4));
        assertEquals(2, atual.getItens().size());
        assertEquals(6, atual.getItens().getFirst().getQuantidade());
        assertEquals(
                2,
                new PedidoPecasRepository(factory)
                        .findById(p.getCodigo())
                        .orElseThrow()
                        .getItens()
                        .size());
        assertThrows(
                DadosInvalidosException.class,
                () ->
                        svc.adicionarItem(
                                p.getCodigo(),
                                new ItemPedidoPecaRequest(outra.getCodigo(), Integer.MAX_VALUE)));
        var estado = pedidos.findById(p.getCodigo()).orElseThrow();
        assertEquals(3, estado.getItens().get(1).getQuantidade());
        assertTrue(pedidos.findAll().stream().anyMatch(v -> v.getCodigo().equals(p.getCodigo())));
        assertTrue(pedidos.deleteById(p.getCodigo()));
        assertTrue(pedidos.findById(p.getCodigo()).isEmpty());
    }

    @Test
    void rollbackDeOrdemNaoDeixaPaiNemAssociacoesParciais() {
        long antes = ordens.findAll().size();
        var s = servico();
        assertThrows(
                ConflitoDadosException.class,
                () ->
                        ordens.save(
                                OrdemServico.builder()
                                        .codigosServicos(List.of(s.getCodigo()))
                                        .codigosPecas(List.of(Long.MAX_VALUE))
                                        .build()));
        assertEquals(antes, ordens.findAll().size());
        assertTrue(servicos.deleteById(s.getCodigo()));
    }

    @Test
    void rollbackDePedidoNaoDeixaCadastroParcial() {
        long antes = pedidos.findAll().size();
        var p = peca();
        assertThrows(
                ConflitoDadosException.class,
                () ->
                        pedidos.save(
                                new PedidoPecas(
                                        List.of(
                                                new ItemPedidoPeca(p.getCodigo(), 2),
                                                new ItemPedidoPeca(Long.MAX_VALUE, 1)))));
        assertEquals(antes, pedidos.findAll().size());
        assertTrue(pecas.deleteById(p.getCodigo()));
    }

    @Test
    void rollbackDeAtualizacaoRestauraItensAnteriores() {
        var p = pedido();
        var data = p.getDataUltimaAtualizacao();
        p.substituirItens(List.of(new ItemPedidoPeca(Long.MAX_VALUE, 1)));
        assertThrows(ConflitoDadosException.class, () -> pedidos.update(p));
        var lido = pedidos.findById(p.getCodigo()).orElseThrow();
        assertEquals(1, lido.getItens().size());
        assertEquals(2, lido.getItens().getFirst().getQuantidade());
        assertEquals(data, lido.getDataUltimaAtualizacao());
    }

    @Test
    void chavesSaoUnicasEntreInstanciasDeRepository() {
        var a = servico();
        var b = new ServicoRepository(factory).save(new Servico("Outro", 10, 10.0));
        servicoIds.add(b.getCodigo());
        assertNotEquals(a.getCodigo(), b.getCodigo());
        assertTrue(b.getCodigo() > a.getCodigo());
    }

    @Test
    void restricaoReferencialImpedeExcluirPecaReferenciada() {
        var o = ordem();
        assertThrows(
                ConflitoDadosException.class,
                () -> pecas.deleteById(o.getCodigosPecas().getFirst()));
        assertTrue(pecas.findById(o.getCodigosPecas().getFirst()).isPresent());
    }

    @Test
    void atualizacaoConcorrenteDesatualizadaRetornaConflito() {
        var s = servico();
        var outra = servicos.findById(s.getCodigo()).orElseThrow();
        s.setCustoTabelado(12.0);
        servicos.update(s);
        outra.setCustoTabelado(15.0);
        assertThrows(ConflitoDadosException.class, () -> servicos.update(outra));
        assertEquals(12.0, servicos.findById(s.getCodigo()).orElseThrow().getCustoTabelado());
    }

    @Test
    void httpComServicesEJdbcRetornaChaveGeradaEJsonDaOat2() throws Exception {
        var mvc =
                MockMvcBuilders.standaloneSetup(new PecaController(new PecaService(pecas))).build();
        var resultado =
                mvc.perform(
                                post("/api/pecas")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"nome\":\"HTTP"
                                                    + " MySQL\",\"categoria\":\"MOTOR\",\"codigo\":-1,"
                                                    + "\"codigoBarras\":\"OAT3\",\"fornecedorMarca\":\"Teste\","
                                                    + "\"quantidadeEstoque\":0,\"precoCusto\":0,\"precoVenda\":0}"))
                        .andExpect(status().isCreated())
                        .andReturn();
        var json =
                JsonMapper.builder().build().readTree(resultado.getResponse().getContentAsString());
        long codigo = json.get("codigo").longValue();
        pecaIds.add(codigo);
        assertTrue(codigo > 0);
        assertTrue(json.has("dataCadastro"));
        assertFalse(json.has("versao"));
        assertTrue(pecas.findById(codigo).isPresent());
        mvc.perform(delete("/api/pecas/" + codigo)).andExpect(status().isNoContent());
        mvc.perform(get("/api/pecas/" + codigo)).andExpect(status().isNotFound());
    }
}
