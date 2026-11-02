package br.com.mecaniQA.api.service;

import static br.com.mecaniQA.api.service.ServiceRules.exigir;
import static br.com.mecaniQA.api.service.ServiceRules.proximo;

import br.com.mecaniQA.api.dto.OrdemServicoRequest;
import br.com.mecaniQA.api.dto.StatusOrdemServicoRequest;
import br.com.mecaniQA.api.mapper.OrdemServicoMapper;
import br.com.mecaniQA.api.model.OrdemServico;
import br.com.mecaniQA.api.model.Peca;
import br.com.mecaniQA.api.model.Servico;
import br.com.mecaniQA.api.model.StatusOrdemServico;
import br.com.mecaniQA.api.repository.Repository;

import org.springframework.stereotype.Service;

@Service
public class OrdemServicoService extends CrudService<OrdemServico> {
    private final Repository<Peca, Long> pecas;
    private final Repository<Servico, Long> servicos;

    public OrdemServicoService(
            Repository<OrdemServico, Long> repository,
            Repository<Peca, Long> pecas,
            Repository<Servico, Long> servicos) {
        super(repository);
        this.pecas = pecas;
        this.servicos = servicos;
    }

    public OrdemServico criar(OrdemServicoRequest requisicao) {
        exigir(requisicao != null, "Informe os dados da ordem.");
        OrdemServico ordem = OrdemServicoMapper.toEntity(requisicao);
        for (Long codigo : ordem.getCodigosPecas()) {
            exigir(pecas.findById(codigo).isPresent(), "Peca inexistente.");
        }
        for (Long codigo : ordem.getCodigosServicos()) {
            exigir(servicos.findById(codigo).isPresent(), "Servico inexistente.");
        }
        return repository.save(ordem);
    }

    public OrdemServico alterarStatus(Long codigo, StatusOrdemServicoRequest requisicao) {
        exigir(requisicao != null, "Informe o status.");
        OrdemServico ordem = buscar(codigo);
        ordem.alterarStatus(
                proximo(ordem.getStatus(), requisicao.status(), StatusOrdemServico.class));
        return repository.update(ordem);
    }
}
