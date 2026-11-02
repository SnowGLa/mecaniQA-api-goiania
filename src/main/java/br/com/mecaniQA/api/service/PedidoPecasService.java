package br.com.mecaniQA.api.service;

import static br.com.mecaniQA.api.service.ServiceRules.exigir;
import static br.com.mecaniQA.api.service.ServiceRules.proximo;

import br.com.mecaniQA.api.dto.ItemPedidoPecaRequest;
import br.com.mecaniQA.api.dto.PedidoPecasRequest;
import br.com.mecaniQA.api.dto.StatusPedidoPecasRequest;
import br.com.mecaniQA.api.exception.DadosInvalidosException;
import br.com.mecaniQA.api.exception.EntidadeNaoEncontradaException;
import br.com.mecaniQA.api.mapper.ItemPedidoPecaMapper;
import br.com.mecaniQA.api.mapper.PedidoPecasMapper;
import br.com.mecaniQA.api.model.Peca;
import br.com.mecaniQA.api.model.PedidoPecas;
import br.com.mecaniQA.api.model.StatusPedidoPecas;
import br.com.mecaniQA.api.repository.Repository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class PedidoPecasService extends CrudService<PedidoPecas> {
    private final Repository<Peca, Long> pecas;

    public PedidoPecasService(
            Repository<PedidoPecas, Long> repository, Repository<Peca, Long> pecas) {
        super(repository);
        this.pecas = pecas;
    }

    private List<ItemPedidoPecaRequest> consolidar(List<ItemPedidoPecaRequest> itens) {
        exigir(itens != null, "Informe a lista de itens.");
        var quantidades = new LinkedHashMap<Long, Integer>();
        for (var item : itens) {
            validarItem(item);
            try {
                quantidades.merge(item.codigoPeca(), item.quantidade(), Math::addExact);
            } catch (ArithmeticException excecao) {
                throw new DadosInvalidosException(
                        "Quantidade total excede o limite permitido.", excecao);
            }
        }
        return quantidades.entrySet().stream()
                .map(entrada -> new ItemPedidoPecaRequest(entrada.getKey(), entrada.getValue()))
                .toList();
    }

    private void validarItem(ItemPedidoPecaRequest item) {
        exigir(
                item != null
                        && item.codigoPeca() != null
                        && item.codigoPeca() > 0
                        && item.quantidade() != null
                        && item.quantidade() > 0,
                "Item invalido.");
    }

    public PedidoPecas criar(PedidoPecasRequest requisicao) {
        exigir(requisicao != null, "Informe os dados do pedido.");
        var itens = consolidar(requisicao.itens());
        for (var item : itens) {
            exigir(pecas.findById(item.codigoPeca()).isPresent(), "Peca inexistente.");
        }
        return repository.save(PedidoPecasMapper.toEntity(new PedidoPecasRequest(itens)));
    }

    public PedidoPecas adicionarItem(Long codigo, ItemPedidoPecaRequest requisicao) {
        exigir(requisicao != null, "Informe o item.");
        PedidoPecas pedido = buscar(codigo);
        validarItem(requisicao);
        if (pecas.findById(requisicao.codigoPeca()).isEmpty()) {
            throw new EntidadeNaoEncontradaException("Peca nao encontrada.");
        }
        List<ItemPedidoPecaRequest> itens =
                new ArrayList<>(
                        pedido.getItens().stream()
                                .map(
                                        i ->
                                                new ItemPedidoPecaRequest(
                                                        i.getCodigoPeca(), i.getQuantidade()))
                                .toList());
        itens.add(requisicao);
        pedido.substituirItens(
                consolidar(itens).stream().map(ItemPedidoPecaMapper::toEntity).toList());
        return repository.update(pedido);
    }

    public PedidoPecas alterarStatus(Long codigo, StatusPedidoPecasRequest requisicao) {
        exigir(requisicao != null, "Informe o status.");
        PedidoPecas pedido = buscar(codigo);
        pedido.alterarStatus(
                proximo(pedido.getStatus(), requisicao.status(), StatusPedidoPecas.class));
        return repository.update(pedido);
    }
}
