package br.com.mecaniQA.api.model;

import java.time.LocalDateTime;

public class Peca {

    private static Long contadorId = 1L;

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


    public Peca(String nome, String codigoBarras, String fornecedorMarca,
                CategoriaPeca categoria, Double precoCusto, Double precoVenda) {
        this.codigo = contadorId++;
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

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getFornecedorMarca() { return fornecedorMarca; }
    public void setFornecedorMarca(String fornecedorMarca) { this.fornecedorMarca = fornecedorMarca; }

    public CategoriaPeca getCategoria() { return categoria; }
    public void setCategoria(CategoriaPeca categoria) {
        this.categoria = categoria;
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    public int getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    public Double getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(Double precoCusto) {
        this.precoCusto = precoCusto;
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
