package br.com.mecaniQA.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.mecaniQA.api.support.TestRepositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class CadastroCatalogoHttpTests {
    private final TestRepositories banco = new TestRepositories();
    private final MockMvc mvc = banco.mvc();
    private final JsonMapper mapper = JsonMapper.builder().build();

    static Map<String, Object> cadastro(String tipo) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("nome", tipo.equals("pecas") ? "Filtro" : "Revisao");
        if (tipo.equals("pecas")) {
            dados.put("codigoBarras", "123");
            dados.put("fornecedorMarca", "Marca");
            dados.put("categoria", "MOTOR");
            dados.put("quantidadeEstoque", 4);
            dados.put("precoCusto", 10.25);
            dados.put("precoVenda", 20.50);
        } else {
            dados.put("tempoEstimadoMinutos", 60);
            dados.put("custoTabelado", 100.25);
        }
        return dados;
    }

    static Stream<Arguments> obrigatorios() {
        return Stream.of("pecas", "servicos")
                .flatMap(
                        tipo ->
                                cadastro(tipo).keySet().stream()
                                        .flatMap(
                                                campo ->
                                                        Stream.of(
                                                                Arguments.of(tipo, campo, false),
                                                                Arguments.of(tipo, campo, true))));
    }

    @ParameterizedTest
    @MethodSource("obrigatorios")
    void rejeitaCadaCampoAusenteOuNuloSemPersistir(String tipo, String campo, boolean nulo)
            throws Exception {
        var dados = cadastro(tipo);
        if (nulo) {
            dados.put(campo, null);
        } else {
            dados.remove(campo);
        }
        rejeitar(tipo, dados);
    }

    static Stream<Arguments> textosInvalidos() {
        return Stream.of("", "   ", "\t\n")
                .flatMap(
                        valor ->
                                Stream.of(
                                        Arguments.of("pecas", "nome", valor),
                                        Arguments.of("pecas", "codigoBarras", valor),
                                        Arguments.of("pecas", "fornecedorMarca", valor),
                                        Arguments.of("servicos", "nome", valor)));
    }

    @ParameterizedTest
    @MethodSource("textosInvalidos")
    void rejeitaTextoVazioOuSomenteEspacos(String tipo, String campo, String valor)
            throws Exception {
        var dados = cadastro(tipo);
        dados.put(campo, valor);
        rejeitar(tipo, dados);
    }

    static Stream<Arguments> negativos() {
        return Stream.of(
                Arguments.of("pecas", "quantidadeEstoque"),
                Arguments.of("pecas", "precoCusto"),
                Arguments.of("pecas", "precoVenda"),
                Arguments.of("servicos", "tempoEstimadoMinutos"),
                Arguments.of("servicos", "custoTabelado"));
    }

    @ParameterizedTest
    @MethodSource("negativos")
    void rejeitaValoresNegativos(String tipo, String campo) throws Exception {
        var dados = cadastro(tipo);
        dados.put(campo, -1);
        rejeitar(tipo, dados);
    }

    static Stream<Arguments> moedasInvalidas() {
        return Stream.of("NaN", "Infinity", "-Infinity", 1000000000000.0)
                .flatMap(
                        valor ->
                                Stream.of(
                                        Arguments.of("pecas", "precoCusto", valor),
                                        Arguments.of("pecas", "precoVenda", valor),
                                        Arguments.of("servicos", "custoTabelado", valor)));
    }

    @ParameterizedTest
    @MethodSource("moedasInvalidas")
    void rejeitaMoedasNaoFinitasOuAcimaDoLimite(String tipo, String campo, Object valor)
            throws Exception {
        var dados = cadastro(tipo);
        dados.put(campo, valor);
        rejeitar(tipo, dados);
    }

    @ParameterizedTest
    @ValueSource(strings = {"pecas", "servicos"})
    void cadastroCompletoRetorna201EPersisteTodosOsCampos(String tipo) throws Exception {
        var dados = cadastro(tipo);
        var resposta = cadastrar(tipo, dados);
        dados.forEach(
                (campo, valor) -> assertEquals(mapper.valueToTree(valor), resposta.get(campo)));
        assertTrue(resposta.get("codigo").longValue() > 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"pecas", "servicos"})
    void aceitaZeroExplicitoParaQuantidadeDuracaoEValores(String tipo) throws Exception {
        var dados = cadastro(tipo);
        List<String> campos =
                tipo.equals("pecas")
                        ? List.of("quantidadeEstoque", "precoCusto", "precoVenda")
                        : List.of("tempoEstimadoMinutos", "custoTabelado");
        campos.forEach(campo -> dados.put(campo, 0));
        var resposta = cadastrar(tipo, dados);
        campos.forEach(campo -> assertEquals(0.0, resposta.get(campo).doubleValue()));
    }

    @Test
    void tamanhoECorAusentesOuNulosContinuamOpcionais() throws Exception {
        var dados = cadastro("pecas");
        var primeira = cadastrar("pecas", dados);
        dados.put("tamanho", null);
        dados.put("cor", null);
        var segunda = cadastrar("pecas", dados);
        for (var resposta : List.of(primeira, segunda)) {
            for (String campo : List.of("tamanho", "cor")) {
                assertTrue(resposta.get(campo) == null || resposta.get(campo).isNull());
            }
        }
        assertEquals(2, banco.pecas.findAll().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"pecas", "servicos"})
    void normalizaTextosObrigatoriosAntesDePersistir(String tipo) throws Exception {
        var dados = cadastro(tipo);
        List<String> campos =
                tipo.equals("pecas")
                        ? List.of("nome", "codigoBarras", "fornecedorMarca")
                        : List.of("nome");
        campos.forEach(campo -> dados.put(campo, "  " + dados.get(campo) + " \t"));
        var resposta = cadastrar(tipo, dados);
        campos.forEach(
                campo ->
                        assertEquals(
                                dados.get(campo).toString().trim(),
                                resposta.get(campo).asString()));
        if (tipo.equals("pecas")) {
            var peca = banco.pecas.findById(resposta.get("codigo").longValue()).orElseThrow();
            assertEquals("Filtro", peca.getNome());
            assertEquals("123", peca.getCodigoBarras());
            assertEquals("Marca", peca.getFornecedorMarca());
        } else {
            assertEquals("Revisao", banco.servicos.findAll().getFirst().getNome());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"pecas", "servicos"})
    void ignoraCodigoEDatasDoCliente(String tipo) throws Exception {
        var dados = cadastro(tipo);
        dados.put("codigo", -1);
        for (String campo : List.of("dataCadastro", "dataCriacao", "dataUltimaAtualizacao")) {
            dados.put(campo, "1900-01-01T00:00:00");
        }
        var resposta = cadastrar(tipo, dados);
        assertTrue(resposta.get("codigo").longValue() > 0);
        for (String campo :
                List.of(
                        tipo.equals("pecas") ? "dataCadastro" : "dataCriacao",
                        "dataUltimaAtualizacao")) {
            assertTrue(LocalDateTime.parse(resposta.get(campo).asString()).getYear() > 1900);
        }
    }

    private JsonNode cadastrar(String tipo, Map<String, Object> dados) throws Exception {
        var resultado =
                mvc.perform(
                                post("/api/" + tipo)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(mapper.writeValueAsString(dados)))
                        .andExpect(status().isCreated())
                        .andReturn();
        return mapper.readTree(resultado.getResponse().getContentAsString());
    }

    private void rejeitar(String tipo, Map<String, Object> dados) throws Exception {
        mvc.perform(
                        post("/api/" + tipo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(dados)))
                .andExpect(status().isBadRequest());
        assertTrue(banco.pecas.findAll().isEmpty());
        assertTrue(banco.servicos.findAll().isEmpty());
    }
}
