package br.com.mecaniQA.api.mapper;

import br.com.mecaniQA.api.dto.PecaRequest;
import br.com.mecaniQA.api.dto.PecaResponse;
import br.com.mecaniQA.api.model.Peca;

public final class PecaMapper {
    private PecaMapper() { }

    public static Peca toEntity(PecaRequest dto) {
        Peca peca = new Peca(dto.nome(), dto.codigoBarras(), dto.fornecedorMarca(),
                dto.categoria(), dto.precoCusto(), dto.precoVenda());
        peca.setQuantidadeEstoque(dto.quantidadeEstoque() == null ? 0 : dto.quantidadeEstoque());
        peca.setTamanho(dto.tamanho());
        peca.setCor(dto.cor());
        return peca;
    }

    public static void atualizar(Peca peca, PecaRequest dto) {
        peca.setPrecoCusto(dto.precoCusto());
        peca.setPrecoVenda(dto.precoVenda());
        peca.setQuantidadeEstoque(dto.quantidadeEstoque() == null ? 0 : dto.quantidadeEstoque());
    }

    public static PecaResponse toResponse(Peca peca) {
        return new PecaResponse(peca.getCodigo(), peca.getNome(), peca.getCodigoBarras(),
                peca.getFornecedorMarca(), peca.getCategoria(), peca.getQuantidadeEstoque(),
                peca.getPrecoCusto(), peca.getPrecoVenda(), peca.getTamanho(), peca.getCor(),
                peca.getDataCadastro(), peca.getDataUltimaAtualizacao());
    }
}
