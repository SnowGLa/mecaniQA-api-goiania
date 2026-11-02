package br.com.mecaniQA.api.service;

import static br.com.mecaniQA.api.service.ServiceRules.exigir;
import static br.com.mecaniQA.api.service.ServiceRules.moeda;
import static br.com.mecaniQA.api.service.ServiceRules.texto;
import static br.com.mecaniQA.api.service.ServiceRules.textoObrigatorio;

import br.com.mecaniQA.api.dto.ServicoRequest;
import br.com.mecaniQA.api.mapper.ServicoMapper;
import br.com.mecaniQA.api.model.Servico;
import br.com.mecaniQA.api.repository.Repository;

import org.springframework.stereotype.Service;

@Service
public class ServicoService extends CrudService<Servico> {
    public ServicoService(Repository<Servico, Long> repository) {
        super(repository);
    }

    private void validar(ServicoRequest requisicao) {
        exigir(requisicao != null, "Informe os dados do servico.");
        exigir(
                requisicao.tempoEstimadoMinutos() == null || requisicao.tempoEstimadoMinutos() >= 0,
                "Tempo invalido.");
        moeda(requisicao.custoTabelado());
        texto(requisicao.nome(), 255);
    }

    public Servico criar(ServicoRequest requisicao) {
        exigir(requisicao != null, "Informe os dados do servico.");
        exigir(requisicao.tempoEstimadoMinutos() != null, "Informe o tempo estimado.");
        exigir(requisicao.custoTabelado() != null, "Informe o custo tabelado.");
        ServicoRequest normalizada =
                new ServicoRequest(
                        textoObrigatorio(requisicao.nome(), 255),
                        requisicao.tempoEstimadoMinutos(),
                        requisicao.custoTabelado());
        validar(normalizada);
        return repository.save(ServicoMapper.toEntity(normalizada));
    }

    public Servico atualizar(Long codigo, ServicoRequest requisicao) {
        validar(requisicao);
        Servico servico = buscar(codigo);
        ServicoMapper.atualizar(servico, requisicao);
        return repository.update(servico);
    }
}
