package br.com.mecaniQA.api;

import static org.junit.jupiter.api.Assertions.*;

import br.com.mecaniQA.api.dto.PecaRequest;
import br.com.mecaniQA.api.dto.ServicoRequest;
import br.com.mecaniQA.api.exception.DadosInvalidosException;
import br.com.mecaniQA.api.model.CategoriaPeca;
import br.com.mecaniQA.api.support.TestRepositories;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class CadastroCatalogoServiceTests {
    static Stream<Arguments> moedasInvalidas() {
        return Stream.of(
                        -1.0,
                        Double.NaN,
                        Double.POSITIVE_INFINITY,
                        Double.NEGATIVE_INFINITY,
                        1000000000000.0)
                .flatMap(
                        valor ->
                                Stream.of("precoCusto", "precoVenda", "custoTabelado")
                                        .map(campo -> Arguments.of(campo, valor)));
    }

    @ParameterizedTest
    @MethodSource("moedasInvalidas")
    void rejeitaMoedaInvalidaAntesDePersistir(String campo, Double valor) {
        var banco = new TestRepositories();
        assertThrows(
                DadosInvalidosException.class,
                () -> {
                    if (campo.equals("custoTabelado")) {
                        banco.servicoService.criar(new ServicoRequest("Revisao", 0, valor));
                    } else {
                        banco.pecaService.criar(
                                new PecaRequest(
                                        "Filtro",
                                        "123",
                                        "Marca",
                                        CategoriaPeca.MOTOR,
                                        0,
                                        campo.equals("precoCusto") ? valor : 0.0,
                                        campo.equals("precoVenda") ? valor : 0.0,
                                        null,
                                        null));
                    }
                });
        assertTrue(banco.pecas.findAll().isEmpty());
        assertTrue(banco.servicos.findAll().isEmpty());
    }
}
