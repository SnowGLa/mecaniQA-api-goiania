package com.mecaniqa.api.model;

import java.time.LocalDateTime;

public class Peca {
    private Long codigo;
    private String nome;
    private String codigoBarras;
    private String fornecedorMarca;
    private CategoriaPeca categoria;
    private int quantidadeEstoque;
    private Double precoCusto;
    private Double precoVenda;
    private String tamanho;
    private String cor;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataUltimaAtualizacao;
}

public Peca(String nome, String codigoBarras, String fornecedorMarca,
            CategoriaPeca categoria, Double precoCusto, Double precoVenda) {
    this.nome = nome;
    this.codigoBarras = codigoBarras;
    this.fornecedorMarca = fornecedorMarca;
    this.categoria = categoria;
    this.precoCusto = precoCusto;
    this.precoVenda = precoVenda;
    this.quantidadeEstoque = 0;
    this.dataCadastro = LocalDateTime.now();
    this.dataUltimaAtualizacao = LocalDateTime.now();
}

public Long getCodigo() { return codigo; }
public void setCodigo(Long codigo) { this.codigo = codigo; }

public String getNome() { return nome; }
public void setNome(String nome) { this.nome = nome; }

public CategoriaPeca getCategoria() { return categoria; }
public void setCategoria(CategoriaPeca categoria) { this.categoria = categoria; }

public int getQuantidadeEstoque() { return quantidadeEstoque; }
public void setQuantidadeEstoque(int quantidadeEstoque) {
    this.quantidadeEstoque = quantidadeEstoque;
    this.dataUltimaAtualizacao = LocalDateTime.now();
}

public Double getPrecoVenda() { return precoVenda; }
public void setPrecoVenda(Double precoVenda) {
    this.precoVenda = precoVenda;
    this.dataUltimaAtualizacao = LocalDateTime.now();
}

public String getTamanho() { return tamanho; }
public void setTamanho(String tamanho) { this.tamanho = tamanho; }

public String getCor() { return cor; }
public void setCor(String cor) { this.cor = cor; }

public LocalDateTime getDataCadastro() { return dataCadastro; }
public LocalDateTime getDataUltimaAtualizacao() { return dataUltimaAtualizacao; }
}

