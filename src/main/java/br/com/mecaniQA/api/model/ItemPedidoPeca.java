package br.com.mecaniQA.api.model;

import br.com.mecaniQA.api.exception.DadosInvalidosException;

public final class ItemPedidoPeca {
    private final Long codigoPeca;
    private final int quantidade;

    public ItemPedidoPeca(Long codigoPeca, int quantidade) {
        if (codigoPeca == null || codigoPeca <= 0 || quantidade <= 0) {
            throw new DadosInvalidosException("Informe peca positiva e quantidade maior que zero.");
        }
        this.codigoPeca = codigoPeca;
        this.quantidade = quantidade;
    }

    public Long getCodigoPeca() {
        return codigoPeca;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
