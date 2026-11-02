package br.com.mecaniQA.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import br.com.mecaniQA.api.model.CategoriaPeca;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PecaRequest(String nome, String codigoBarras, String fornecedorMarca, CategoriaPeca categoria,
                          Integer quantidadeEstoque, Double precoCusto, Double precoVenda, String tamanho, String cor) {
}
