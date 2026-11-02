package br.com.mecaniQA.api.support;

import br.com.mecaniQA.api.controller.OrdemServicoController;
import br.com.mecaniQA.api.controller.PecaController;
import br.com.mecaniQA.api.controller.PedidoPecasController;
import br.com.mecaniQA.api.controller.ServicoController;
import br.com.mecaniQA.api.model.OrdemServico;
import br.com.mecaniQA.api.model.Peca;
import br.com.mecaniQA.api.model.PedidoPecas;
import br.com.mecaniQA.api.model.Servico;
import br.com.mecaniQA.api.service.OrdemServicoService;
import br.com.mecaniQA.api.service.PecaService;
import br.com.mecaniQA.api.service.PedidoPecasService;
import br.com.mecaniQA.api.service.ServicoService;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class TestRepositories {
    public final MemoryRepository<Peca> pecas =
            new MemoryRepository<>(
                    (e, id, v) ->
                            Peca.reconstituir(
                                    id,
                                    e.getNome(),
                                    e.getCodigoBarras(),
                                    e.getFornecedorMarca(),
                                    e.getCategoria(),
                                    e.getQuantidadeEstoque(),
                                    e.getPrecoCusto(),
                                    e.getPrecoVenda(),
                                    e.getTamanho(),
                                    e.getCor(),
                                    e.getDataCadastro(),
                                    e.getDataUltimaAtualizacao(),
                                    v));
    public final MemoryRepository<Servico> servicos =
            new MemoryRepository<>(
                    (e, id, v) ->
                            Servico.reconstituir(
                                    id,
                                    e.getNome(),
                                    e.getTempoEstimadoMinutos(),
                                    e.getCustoTabelado(),
                                    e.getDataCriacao(),
                                    e.getDataUltimaAtualizacao(),
                                    v));
    public final MemoryRepository<OrdemServico> ordens =
            new MemoryRepository<>(
                    (e, id, v) ->
                            OrdemServico.reconstituir(
                                    id,
                                    e.getStatus(),
                                    e.getCodigosServicos(),
                                    e.getCodigosPecas(),
                                    e.getDataCriacao(),
                                    e.getDataUltimaAtualizacao(),
                                    v));
    public final MemoryRepository<PedidoPecas> pedidos =
            new MemoryRepository<>(
                    (e, id, v) ->
                            PedidoPecas.reconstituir(
                                    id,
                                    e.getStatus(),
                                    e.getItens(),
                                    e.getDataCriacao(),
                                    e.getDataUltimaAtualizacao(),
                                    v));
    public final PecaService pecaService = new PecaService(pecas);
    public final ServicoService servicoService = new ServicoService(servicos);
    public final OrdemServicoService ordemService =
            new OrdemServicoService(ordens, pecas, servicos);
    public final PedidoPecasService pedidoService = new PedidoPecasService(pedidos, pecas);

    public MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(
                        new PecaController(pecaService),
                        new ServicoController(servicoService),
                        new OrdemServicoController(ordemService),
                        new PedidoPecasController(pedidoService))
                .build();
    }
}
