package br.com.mecaniQA.api.support;

import br.com.mecaniQA.api.model.OrdemServico;
import br.com.mecaniQA.api.model.Peca;
import br.com.mecaniQA.api.model.PedidoPecas;
import br.com.mecaniQA.api.model.Servico;
import br.com.mecaniQA.api.repository.Repository;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestRepositoryConfiguration {
    private final TestRepositories banco = new TestRepositories();

    @Bean
    @Primary
    public Repository<Peca, Long> pecaTestRepository() {
        return banco.pecas;
    }

    @Bean
    @Primary
    public Repository<Servico, Long> servicoTestRepository() {
        return banco.servicos;
    }

    @Bean
    @Primary
    public Repository<OrdemServico, Long> ordemTestRepository() {
        return banco.ordens;
    }

    @Bean
    @Primary
    public Repository<PedidoPecas, Long> pedidoTestRepository() {
        return banco.pedidos;
    }
}
