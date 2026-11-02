package br.com.mecaniQA.api.model;

import java.time.LocalDateTime;

public class Peca extends EntidadePersistente {
    private String nome;
    private String codigoBarras;
    private String fornecedorMarca;
    private CategoriaPeca categoria;
    private int quantidadeEstoque;
    private Double precoCusto;
    private Double precoVenda;
    private String tamanho;
    private String cor;

    public Peca() {}

    public Peca(
            String nome,
            String codigoBarras,
            String fornecedorMarca,
            CategoriaPeca categoria,
            Double precoCusto,
            Double precoVenda) {
        this.nome = nome;
        this.codigoBarras = codigoBarras;
        this.fornecedorMarca = fornecedorMarca;
        this.categoria = categoria;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
    }

    private Peca(
            Long codigo,
            String nome,
            String codigoBarras,
            String fornecedorMarca,
            CategoriaPeca categoria,
            int quantidadeEstoque,
            Double precoCusto,
            Double precoVenda,
            String tamanho,
            String cor,
            LocalDateTime criacao,
            LocalDateTime atualizacao,
            long versao) {
        super(codigo, criacao, atualizacao, versao);
        this.nome = nome;
        this.codigoBarras = codigoBarras;
        this.fornecedorMarca = fornecedorMarca;
        this.categoria = categoria;
        this.quantidadeEstoque = quantidadeEstoque;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.tamanho = tamanho;
        this.cor = cor;
    }

    public static Peca reconstituir(
            Long codigo,
            String nome,
            String codigoBarras,
            String fornecedorMarca,
            CategoriaPeca categoria,
            int quantidadeEstoque,
            Double precoCusto,
            Double precoVenda,
            String tamanho,
            String cor,
            LocalDateTime criacao,
            LocalDateTime atualizacao,
            long versao) {
        return new Peca(
                codigo,
                nome,
                codigoBarras,
                fornecedorMarca,
                categoria,
                quantidadeEstoque,
                precoCusto,
                precoVenda,
                tamanho,
                cor,
                criacao,
                atualizacao,
                versao);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
        registrarAlteracao();
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
        registrarAlteracao();
    }

    public String getFornecedorMarca() {
        return fornecedorMarca;
    }

    public void setFornecedorMarca(String fornecedorMarca) {
        this.fornecedorMarca = fornecedorMarca;
        registrarAlteracao();
    }

    public CategoriaPeca getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaPeca categoria) {
        this.categoria = categoria;
        registrarAlteracao();
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
        registrarAlteracao();
    }

    public Double getPrecoCusto() {
        return precoCusto;
    }

    public void setPrecoCusto(Double precoCusto) {
        this.precoCusto = precoCusto;
        registrarAlteracao();
    }

    public Double getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(Double precoVenda) {
        this.precoVenda = precoVenda;
        registrarAlteracao();
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
        registrarAlteracao();
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
        registrarAlteracao();
    }

    public LocalDateTime getDataCadastro() {
        return dataCriacao;
    }
}
