package br.com.mecaniQA.api.dto;

import br.com.mecaniQA.api.model.CategoriaPeca;
import java.time.LocalDateTime;

public record PecaResponse(Long codigo, String nome, String codigoBarras, String fornecedorMarca,
                           CategoriaPeca categoria, int quantidadeEstoque, Double precoCusto, Double precoVenda,
                           String tamanho, String cor, LocalDateTime dataCadastro, LocalDateTime dataUltimaAtualizacao) {
}
