package br.com.mecaniQA.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.mecaniQA.api.controller.OrdemServicoController;
import br.com.mecaniQA.api.controller.PecaController;
import br.com.mecaniQA.api.controller.PedidoPecasController;
import br.com.mecaniQA.api.controller.ServicoController;
import br.com.mecaniQA.api.dto.ItemPedidoPecaRequest;
import br.com.mecaniQA.api.dto.OrdemServicoRequest;
import br.com.mecaniQA.api.dto.PecaRequest;
import br.com.mecaniQA.api.dto.PedidoPecasRequest;
import br.com.mecaniQA.api.dto.ServicoRequest;
import br.com.mecaniQA.api.dto.StatusOrdemServicoRequest;
import br.com.mecaniQA.api.exception.ConflitoDadosException;
import br.com.mecaniQA.api.exception.DadosInvalidosException;
import br.com.mecaniQA.api.exception.EntidadeNaoEncontradaException;
import br.com.mecaniQA.api.exception.PersistenciaException;
import br.com.mecaniQA.api.jdbc.AbstractJdbcRepository;
import br.com.mecaniQA.api.model.CategoriaPeca;
import br.com.mecaniQA.api.model.OrdemServico;
import br.com.mecaniQA.api.model.Peca;
import br.com.mecaniQA.api.model.PedidoPecas;
import br.com.mecaniQA.api.model.Servico;
import br.com.mecaniQA.api.model.StatusOrdemServico;
import br.com.mecaniQA.api.repository.OrdemServicoRepository;
import br.com.mecaniQA.api.repository.PecaRepository;
import br.com.mecaniQA.api.repository.PedidoPecasRepository;
import br.com.mecaniQA.api.repository.Repository;
import br.com.mecaniQA.api.repository.ServicoRepository;
import br.com.mecaniQA.api.support.TestRepositories;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.JavacTask;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.ToolProvider;

class ArchitectureServiceTests {
    @Test
    void contratoGenericoTemExatamenteAsCincoOperacoes() {
        assertEquals(2, Repository.class.getTypeParameters().length);
        assertEquals(
                Set.of("save", "findById", "findAll", "update", "deleteById"),
                Arrays.stream(Repository.class.getDeclaredMethods())
                        .map(m -> m.getName())
                        .collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void quatroRepositoriesImplementamContratoEHerdamTemplateSemInstanciaEstatica() {
        for (var tipo :
                List.of(
                        PecaRepository.class,
                        ServicoRepository.class,
                        OrdemServicoRepository.class,
                        PedidoPecasRepository.class)) {
            assertTrue(Repository.class.isAssignableFrom(tipo));
            assertEquals(AbstractJdbcRepository.class, tipo.getSuperclass());
            assertEquals(1, tipo.getConstructors().length);
            assertTrue(
                    Arrays.stream(tipo.getDeclaredFields())
                            .noneMatch(f -> Modifier.isStatic(f.getModifiers())));
            assertTrue(
                    Arrays.stream(tipo.getDeclaredMethods())
                            .noneMatch(m -> m.getName().equals("get" + "Instance")));
        }
        for (var metodo : AbstractJdbcRepository.class.getDeclaredMethods()) {
            if (!metodo.isBridge()
                    && Set.of("save", "findById", "findAll", "update", "deleteById")
                            .contains(metodo.getName())) {
                assertTrue(Modifier.isFinal(metodo.getModifiers()));
            }
        }
    }

    @Test
    void fronteiraControllerRecebeSomenteServiceEFontesRespeitamCamadas() throws Exception {
        for (var tipo :
                List.of(
                        PecaController.class,
                        ServicoController.class,
                        OrdemServicoController.class,
                        PedidoPecasController.class)) {
            assertEquals(1, tipo.getConstructors()[0].getParameterCount());
            assertEquals(
                    "br.com.mecaniQA.api.service",
                    tipo.getConstructors()[0].getParameterTypes()[0].getPackageName());
            for (var campo : tipo.getDeclaredFields()) {
                assertFalse(Repository.class.isAssignableFrom(campo.getType()));
            }
        }
        Path root = Path.of("src/main/java/br/com/mecaniQA/api");
        String nomeRemovido = "Controller" + "Responses";
        assertFalse(Files.exists(root.resolve("controller").resolve(nomeRemovido + ".java")));
        try (var arquivos = Files.walk(root)) {
            for (var arquivo : arquivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                String texto = Files.readString(arquivo),
                        relativo = root.relativize(arquivo).toString().replace('\\', '/');
                if (relativo.startsWith("controller/")) {
                    assertFalse(texto.contains("repository"));
                    assertFalse(texto.contains(nomeRemovido));
                    var compilador = ToolProvider.getSystemJavaCompiler();
                    try (var arquivosJava = compilador.getStandardFileManager(null, null, null)) {
                        var tarefa =
                                (JavacTask)
                                        compilador.getTask(
                                                null,
                                                arquivosJava,
                                                null,
                                                List.of("-proc:none"),
                                                null,
                                                arquivosJava.getJavaFileObjects(arquivo.toFile()));
                        for (var unidade : tarefa.parse()) {
                            for (var declaracao : unidade.getTypeDecls()) {
                                if (!(declaracao instanceof ClassTree classe)) {
                                    continue;
                                }
                                for (var membro : classe.getMembers()) {
                                    if (!(membro instanceof MethodTree metodo)
                                            || metodo.getReturnType() == null) {
                                        continue;
                                    }
                                    var tratamentos =
                                            metodo.getBody().getStatements().stream()
                                                    .filter(TryTree.class::isInstance)
                                                    .map(TryTree.class::cast)
                                                    .toList();
                                    assertEquals(
                                            1, tratamentos.size(), metodo.getName().toString());
                                    var excecoes =
                                            tratamentos.getFirst().getCatches().stream()
                                                    .map(c -> c.getParameter().getType().toString())
                                                    .collect(java.util.stream.Collectors.toSet());
                                    assertEquals(
                                            Set.of(
                                                    "DadosInvalidosException",
                                                    "EntidadeNaoEncontradaException",
                                                    "ConflitoDadosException",
                                                    "PersistenciaException"),
                                            excecoes);
                                }
                            }
                        }
                    }
                }
                if (relativo.startsWith("service/")) {
                    assertFalse(
                            texto.matches("(?s).*(java\\.sql|SELECT |INSERT |UPDATE |DELETE ).*"));
                }
                if (texto.contains("java.sql")) {
                    assertTrue(
                            relativo.startsWith("jdbc/") || relativo.startsWith("repository/"),
                            relativo);
                }
                if (relativo.startsWith("model/") || relativo.startsWith("repository/")) {
                    assertFalse(texto.contains("Atomic" + "Long"));
                    assertFalse(texto.contains("get" + "Instance("));
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"pecas", "servicos", "ordens-servico", "pedidos-pecas"})
    void respostasManuais404409500NaoRevelamDetalhes(String rota) throws Exception {
        var banco = new TestRepositories();
        var mvc = banco.mvc();
        assertEquals(
                "",
                mvc.perform(get("/api/" + rota + "/123"))
                        .andExpect(status().isNotFound())
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
        var referencia = banco.pecas.save(new Peca());
        var alvo =
                switch (rota) {
                    case "pecas" -> banco.pecas;
                    case "servicos" -> banco.servicos;
                    case "ordens-servico" -> banco.ordens;
                    default -> banco.pedidos;
                };
        var falhas =
                Map.<Integer, RuntimeException>of(
                        400, new DadosInvalidosException("SQL URL usuario senha stacktrace"),
                        404, new EntidadeNaoEncontradaException("Detalhes internos"),
                        409, new ConflitoDadosException("Detalhes de chave interna"),
                        500, new PersistenciaException("SQL usuario senha jdbc:mysql://interno"));
        String caminho = "/api/" + rota;
        String cadastro =
                switch (rota) {
                    case "pecas" ->
                            "{\"nome\":\"P\",\"codigoBarras\":\"1\",\"fornecedorMarca\":\"M\",\"categoria\":\"MOTOR\",\"quantidadeEstoque\":0,\"precoCusto\":0,\"precoVenda\":0}";
                    case "servicos" ->
                            "{\"nome\":\"S\",\"tempoEstimadoMinutos\":0,\"custoTabelado\":0}";
                    case "ordens-servico" -> "{\"codigosPecas\":[" + referencia.getCodigo() + "]}";
                    default -> "{\"itens\":[]}";
                };
        for (var falha : falhas.entrySet()) {
            alvo.falha = falha.getValue();
            var requisicoes =
                    new ArrayList<
                            org.springframework.test.web.servlet.request
                                    .MockHttpServletRequestBuilder>();
            requisicoes.add(get(caminho));
            requisicoes.add(get(caminho + "/123"));
            requisicoes.add(
                    MockMvcRequestBuilders.post(caminho)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cadastro));
            requisicoes.add(MockMvcRequestBuilders.delete(caminho + "/123"));
            if (rota.equals("pecas") || rota.equals("servicos")) {
                requisicoes.add(
                        MockMvcRequestBuilders.put(caminho + "/123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"));
            } else {
                requisicoes.add(
                        MockMvcRequestBuilders.put(caminho + "/123/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"PENDENTE_PAGAMENTO\"}"));
            }
            if (rota.equals("pedidos-pecas")) {
                requisicoes.add(
                        MockMvcRequestBuilders.post(caminho + "/123/itens")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"codigoPeca\":"
                                                + referencia.getCodigo()
                                                + ",\"quantidade\":1}"));
            }
            for (var requisicao : requisicoes) {
                assertEquals(
                        "",
                        mvc.perform(requisicao)
                                .andExpect(status().is(falha.getKey()))
                                .andReturn()
                                .getResponse()
                                .getContentAsString());
            }
        }
    }

    @Test
    void servicesPersistemEAjudamAReconstituirCodigosDatasEVersao() {
        var banco = new TestRepositories();
        var peca =
                banco.pecaService.criar(
                        new PecaRequest(
                                "P", "1", "M", CategoriaPeca.MOTOR, 2, 1.0, 2.0, null, null));
        var servico = banco.servicoService.criar(new ServicoRequest("S", 30, 10.0));
        assertNotNull(peca.getCodigo());
        assertNotNull(servico.getCodigo());
        var ordem =
                banco.ordemService.criar(
                        new OrdemServicoRequest(
                                List.of(servico.getCodigo()), List.of(peca.getCodigo())));
        var pedido =
                banco.pedidoService.criar(
                        new PedidoPecasRequest(
                                List.of(
                                        new ItemPedidoPecaRequest(peca.getCodigo(), 2),
                                        new ItemPedidoPecaRequest(peca.getCodigo(), 3))));
        assertEquals(5, pedido.getItens().getFirst().getQuantidade());
        assertEquals(0, ordem.getVersao());
        var atualizada =
                banco.ordemService.alterarStatus(
                        ordem.getCodigo(), new StatusOrdemServicoRequest("PENDENTE_PAGAMENTO"));
        assertEquals(1, atualizada.getVersao());
        assertEquals(ordem.getDataCriacao(), atualizada.getDataCriacao());
        assertThrows(ConflitoDadosException.class, () -> banco.ordens.update(ordem));
        banco.ordemService.excluir(ordem.getCodigo());
        banco.pedidoService.excluir(pedido.getCodigo());
        banco.pecaService.excluir(peca.getCodigo());
        banco.servicoService.excluir(servico.getCodigo());
        assertTrue(banco.ordemService.listar().isEmpty());
        assertTrue(banco.pedidoService.listar().isEmpty());
    }

    @Test
    void transicoesSaoValidadasNoServiceSemModificarEstadoNaFalha() {
        var banco = new TestRepositories();
        var peca = banco.pecas.save(new Peca());
        var ordem =
                banco.ordemService.criar(
                        new OrdemServicoRequest(List.of(), List.of(peca.getCodigo())));
        for (var atual : StatusOrdemServico.values()) {
            for (var tentativa : StatusOrdemServico.values()) {
                if (tentativa.ordinal() != atual.ordinal() + 1) {
                    assertThrows(
                            DadosInvalidosException.class,
                            () ->
                                    banco.ordemService.alterarStatus(
                                            ordem.getCodigo(),
                                            new StatusOrdemServicoRequest(tentativa.name())));
                }
            }
            if (atual != StatusOrdemServico.EXECUTADO) {
                banco.ordemService.alterarStatus(
                        ordem.getCodigo(),
                        new StatusOrdemServicoRequest(
                                StatusOrdemServico.values()[atual.ordinal() + 1].name()));
            }
        }
    }

    @Test
    void reconstituicaoDeModeloPreservaIdentidadeSemSettersPublicosDeControle() {
        var data = LocalDateTime.of(2019, 1, 1, 0, 0);
        var s = Servico.reconstituir(999L, "S", 1, 2.0, data, data, 4);
        assertEquals(999L, s.getCodigo());
        assertEquals(data, s.getDataCriacao());
        assertEquals(4, s.getVersao());
        assertNull(new Servico().getCodigo());
        assertNull(new Peca().getCodigo());
        for (var tipo : List.of(Peca.class, Servico.class, OrdemServico.class, PedidoPecas.class)) {
            assertTrue(
                    Arrays.stream(tipo.getMethods())
                            .noneMatch(m -> m.getName().matches("set(Codigo|Data.*|Versao)")));
        }
    }
}
