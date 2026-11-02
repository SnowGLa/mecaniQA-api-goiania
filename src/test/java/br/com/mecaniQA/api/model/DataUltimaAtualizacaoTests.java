package br.com.mecaniQA.api.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

class DataUltimaAtualizacaoTests {

    @Test
    void todosOsSettersDePecaAtualizamData() throws Exception {
        Peca peca = new Peca();
        List<Consumer<Peca>> alteracoes =
                List.of(
                        p -> p.setNome("Filtro"),
                        p -> p.setCodigoBarras("123"),
                        p -> p.setFornecedorMarca("Marca"),
                        p -> p.setCategoria(CategoriaPeca.MOTOR),
                        p -> p.setQuantidadeEstoque(4),
                        p -> p.setPrecoCusto(10.0),
                        p -> p.setPrecoVenda(20.0),
                        p -> p.setTamanho("M"),
                        p -> p.setCor("Preto"));
        verificarAlteracoes(peca, alteracoes, peca::getDataUltimaAtualizacao);
    }

    @Test
    void todosOsSettersDeServicoAtualizamData() throws Exception {
        Servico servico = new Servico();
        List<Consumer<Servico>> alteracoes =
                List.of(
                        s -> s.setNome("Revisao"),
                        s -> s.setTempoEstimadoMinutos(60),
                        s -> s.setCustoTabelado(100.0));
        verificarAlteracoes(servico, alteracoes, servico::getDataUltimaAtualizacao);
    }

    private <T> void verificarAlteracoes(
            T entidade, List<Consumer<T>> alteracoes, Supplier<LocalDateTime> obterData)
            throws Exception {
        var campo = entidade.getClass().getSuperclass().getDeclaredField("dataUltimaAtualizacao");
        campo.setAccessible(true);
        for (Consumer<T> alteracao : alteracoes) {
            // Uma data antiga evita depender da resolucao do relogio ou usar sleeps.
            campo.set(entidade, LocalDateTime.of(1900, 1, 1, 0, 0));
            LocalDateTime inicio = LocalDateTime.now();
            alteracao.accept(entidade);
            assertFalse(obterData.get().isBefore(inicio));
            assertFalse(obterData.get().isAfter(LocalDateTime.now()));
        }
    }
}
