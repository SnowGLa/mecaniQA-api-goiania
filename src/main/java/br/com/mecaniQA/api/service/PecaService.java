package br.com.mecaniQA.api.service;

import static br.com.mecaniQA.api.service.ServiceRules.exigir;
import static br.com.mecaniQA.api.service.ServiceRules.moeda;
import static br.com.mecaniQA.api.service.ServiceRules.texto;
import static br.com.mecaniQA.api.service.ServiceRules.textoObrigatorio;

import br.com.mecaniQA.api.dto.PecaRequest;
import br.com.mecaniQA.api.mapper.PecaMapper;
import br.com.mecaniQA.api.model.Peca;
import br.com.mecaniQA.api.repository.Repository;

import org.springframework.stereotype.Service;

@Service
public class PecaService extends CrudService<Peca> {
    public PecaService(Repository<Peca, Long> repository) {
        super(repository);
    }

    private void validar(PecaRequest requisicao) {
        exigir(requisicao != null, "Informe os dados da peca.");
        exigir(
                requisicao.quantidadeEstoque() == null || requisicao.quantidadeEstoque() >= 0,
                "Estoque invalido.");
        moeda(requisicao.precoCusto());
        moeda(requisicao.precoVenda());
        texto(requisicao.nome(), 255);
        texto(requisicao.codigoBarras(), 255);
        texto(requisicao.fornecedorMarca(), 255);
        texto(requisicao.cor(), 100);
        texto(requisicao.tamanho(), 100);
    }

    public Peca criar(PecaRequest requisicao) {
        exigir(requisicao != null, "Informe os dados da peca.");
        exigir(requisicao.quantidadeEstoque() != null, "Informe o estoque.");
        exigir(requisicao.precoCusto() != null, "Informe o preco de custo.");
        exigir(requisicao.precoVenda() != null, "Informe o preco de venda.");
        exigir(requisicao.categoria() != null, "Informe a categoria.");
        PecaRequest normalizada =
                new PecaRequest(
                        textoObrigatorio(requisicao.nome(), 255),
                        textoObrigatorio(requisicao.codigoBarras(), 255),
                        textoObrigatorio(requisicao.fornecedorMarca(), 255),
                        requisicao.categoria(),
                        requisicao.quantidadeEstoque(),
                        requisicao.precoCusto(),
                        requisicao.precoVenda(),
                        requisicao.tamanho(),
                        requisicao.cor());
        validar(normalizada);
        return repository.save(PecaMapper.toEntity(normalizada));
    }

    public Peca atualizar(Long codigo, PecaRequest requisicao) {
        validar(requisicao);
        Peca peca = buscar(codigo);
        PecaMapper.atualizar(peca, requisicao);
        return repository.update(peca);
    }
}
