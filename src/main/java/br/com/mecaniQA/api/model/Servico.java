package br.com.mecaniQA.api.model;

import java.time.LocalDateTime;

public class Servico {

    private Long codigo;
    private String nome;
    private int tempoEstimadoMinutos;
    private Double custoTabelado;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAtualizacao;

    public Servico(String nome, int tempoEstimadoMinutos, Double custoTabelado) {
        this.nome = nome;
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
        this.custoTabelado = custoTabelado;
        this.dataCriacao = LocalDateTime.now();
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    public Long getCodigo() { return codigo; }
    public void setCodigo(Long codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getTempoEstimadoMinutos() { return tempoEstimadoMinutos; }
    public void setTempoEstimadoMinutos(int tempoEstimadoMinutos) {
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    public Double getCustoTabelado() { return custoTabelado; }
    public void setCustoTabelado(Double custoTabelado) {
        this.custoTabelado = custoTabelado;
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataUltimaAtualizacao() { return dataUltimaAtualizacao; }
}