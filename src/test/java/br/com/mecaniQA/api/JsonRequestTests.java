package br.com.mecaniQA.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.context.annotation.Import(
        br.com.mecaniQA.api.support.TestRepositoryConfiguration.class)
class JsonRequestTests {

    @LocalServerPort private int port;

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void validaCrudDePecasEProtegePropriedadesGeradas() throws Exception {
        verificarRequisicoes(
                "/api/pecas",
                """
                {"codigo":-1,"nome":"Filtro","codigoBarras":"123",
                 "fornecedorMarca":"Marca","categoria":"MOTOR",
                 "quantidadeEstoque":4,"precoCusto":10.0,"precoVenda":20.0,
                 "tamanho":"M","cor":"Preto","dataCadastro":"1900-01-01T00:00:00",
                 "dataUltimaAtualizacao":"1900-01-01T00:00:00"}
                """,
                """
{"codigo":-1,"quantidadeEstoque":8,"precoCusto":12.0,"precoVenda":25.0,
 "dataCadastro":"1900-01-01T00:00:00","dataUltimaAtualizacao":"1900-01-01T00:00:00"}
""",
                "quantidadeEstoque",
                8,
                "dataCadastro");
    }

    @Test
    void validaCrudDeServicosEProtegePropriedadesGeradas() throws Exception {
        verificarRequisicoes(
                "/api/servicos",
                """
                {"codigo":-1,"nome":"Revisao","tempoEstimadoMinutos":60,"custoTabelado":100.0,
                 "dataCriacao":"1900-01-01T00:00:00","dataUltimaAtualizacao":"1900-01-01T00:00:00"}
                """,
                """
                {"codigo":-1,"tempoEstimadoMinutos":90,"custoTabelado":150.0,
                 "dataCriacao":"1900-01-01T00:00:00","dataUltimaAtualizacao":"1900-01-01T00:00:00"}
                """,
                "tempoEstimadoMinutos",
                90,
                "dataCriacao");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", ",\"categoria\":null"})
    void rejeitaPecaSemCategoria(String json) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            var antes = enviar(client, "GET", "/api/pecas", null);
            assertEquals(200, antes.statusCode(), antes.body());
            var resposta =
                    enviar(
                            client,
                            "POST",
                            "/api/pecas",
                            "{\"nome\":\"Filtro\",\"codigoBarras\":\"123\",\"fornecedorMarca\":\"Marca\",\"quantidadeEstoque\":0,\"precoCusto\":0,\"precoVenda\":0"
                                    + json
                                    + "}");
            assertEquals(400, resposta.statusCode(), resposta.body());
            var depois = enviar(client, "GET", "/api/pecas", null);
            assertEquals(200, depois.statusCode(), depois.body());
            assertEquals(mapper.readTree(antes.body()), mapper.readTree(depois.body()));
        }
    }

    private void verificarRequisicoes(
            String caminho,
            String criacao,
            String atualizacao,
            String campoAtualizado,
            int valorEsperado,
            String campoDataCriacao)
            throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            assertEquals(404, enviar(client, "GET", caminho + "/-1", null).statusCode());
            JsonNode primeiro = criar(client, caminho, criacao);
            String primeiroCaminho = caminho + "/" + primeiro.get("codigo").longValue();
            try {
                JsonNode segundo = criar(client, caminho, criacao);
                String segundoCaminho = caminho + "/" + segundo.get("codigo").longValue();
                try {
                    assertNotEquals(primeiro.get("codigo"), segundo.get("codigo"));
                    Set<String> somenteLeitura =
                            Set.of("codigo", campoDataCriacao, "dataUltimaAtualizacao");
                    JsonNode enviado = mapper.readTree(criacao);
                    enviado.properties()
                            .forEach(
                                    campo -> {
                                        if (!somenteLeitura.contains(campo.getKey())) {
                                            assertEquals(
                                                    campo.getValue(),
                                                    primeiro.get(campo.getKey()),
                                                    campo.getKey());
                                        }
                                    });
                    for (String campo : somenteLeitura) {
                        assertNotNull(primeiro.get(campo));
                        assertFalse(primeiro.get(campo).isNull());
                        assertNotEquals(enviado.get(campo), primeiro.get(campo), campo);
                    }

                    var listagem = enviar(client, "GET", caminho, null);
                    assertEquals(200, listagem.statusCode(), listagem.body());
                    JsonNode lista = mapper.readTree(listagem.body());
                    assertTrue(lista.isArray());
                    assertTrue(lista.valueStream().anyMatch(primeiro::equals));
                    assertTrue(lista.valueStream().anyMatch(segundo::equals));

                    var consulta = enviar(client, "GET", primeiroCaminho, null);
                    assertEquals(200, consulta.statusCode(), consulta.body());
                    assertEquals(primeiro, mapper.readTree(consulta.body()));

                    var resposta = enviar(client, "PUT", primeiroCaminho, atualizacao);
                    assertEquals(200, resposta.statusCode(), resposta.body());
                    JsonNode atualizado = mapper.readTree(resposta.body());
                    assertEquals(primeiro.get("codigo"), atualizado.get("codigo"));
                    assertEquals(primeiro.get(campoDataCriacao), atualizado.get(campoDataCriacao));
                    assertNotEquals(
                            mapper.readTree(atualizacao).get("dataUltimaAtualizacao"),
                            atualizado.get("dataUltimaAtualizacao"));
                    LocalDateTime antes =
                            LocalDateTime.parse(
                                    mapper.treeToValue(
                                            primeiro.get("dataUltimaAtualizacao"), String.class));
                    LocalDateTime depois =
                            LocalDateTime.parse(
                                    mapper.treeToValue(
                                            atualizado.get("dataUltimaAtualizacao"), String.class));
                    assertFalse(depois.isBefore(antes));
                    assertEquals(valorEsperado, atualizado.get(campoAtualizado).intValue());
                    var exclusao = enviar(client, "DELETE", primeiroCaminho, null);
                    assertEquals(204, exclusao.statusCode(), exclusao.body());
                    assertEquals(404, enviar(client, "GET", primeiroCaminho, null).statusCode());
                } finally {
                    enviar(client, "DELETE", segundoCaminho, null);
                }
            } finally {
                enviar(client, "DELETE", primeiroCaminho, null);
            }
        }
    }

    private JsonNode criar(HttpClient client, String caminho, String json) throws Exception {
        var resposta = enviar(client, "POST", caminho, json);
        assertEquals(201, resposta.statusCode(), resposta.body());
        JsonNode resultado = mapper.readTree(resposta.body());
        assertNotNull(resultado.get("codigo"));
        assertTrue(resultado.get("codigo").longValue() > 0);
        return resultado;
    }

    private HttpResponse<String> enviar(
            HttpClient client, String metodo, String caminho, String json) throws Exception {
        var corpo =
                json == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(json);
        var request =
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + caminho))
                        .header("Content-Type", "application/json")
                        .method(metodo, corpo)
                        .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
