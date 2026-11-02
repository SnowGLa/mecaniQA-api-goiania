package br.com.mecaniQA.api.model;

import br.com.mecaniQA.api.exception.DadosInvalidosException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class PedidoPecas extends EntidadePersistente {
    private StatusPedidoPecas status;
    private List<ItemPedidoPeca> itens;

    public PedidoPecas(List<ItemPedidoPeca> itens) {
        this.itens = copiar(itens);
        status = StatusPedidoPecas.ORCANDO;
    }

    private PedidoPecas(
            Long codigo,
            StatusPedidoPecas status,
            List<ItemPedidoPeca> itens,
            LocalDateTime criacao,
            LocalDateTime atualizacao,
            long versao) {
        super(codigo, criacao, atualizacao, versao);
        this.status = Objects.requireNonNull(status);
        this.itens = copiar(itens);
    }

    private static List<ItemPedidoPeca> copiar(List<ItemPedidoPeca> itens) {
        if (itens == null || itens.stream().anyMatch(Objects::isNull)) {
            throw new DadosInvalidosException("Informe lista de itens valida.");
        }
        return List.copyOf(itens);
    }

    public static PedidoPecas reconstituir(
            Long codigo,
            StatusPedidoPecas status,
            List<ItemPedidoPeca> itens,
            LocalDateTime criacao,
            LocalDateTime atualizacao,
            long versao) {
        return new PedidoPecas(codigo, status, itens, criacao, atualizacao, versao);
    }

    public StatusPedidoPecas getStatus() {
        return status;
    }

    public List<ItemPedidoPeca> getItens() {
        return itens;
    }

    public void substituirItens(List<ItemPedidoPeca> itens) {
        this.itens = copiar(itens);
        registrarAlteracao();
    }

    public void alterarStatus(StatusPedidoPecas status) {
        this.status = Objects.requireNonNull(status);
        registrarAlteracao();
    }
}
