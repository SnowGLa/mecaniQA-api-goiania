package br.com.mecaniQA.api.model;

import java.time.LocalDateTime;

public class Servico extends EntidadePersistente {
    private String nome;
    private int tempoEstimadoMinutos;
    private Double custoTabelado;

    public Servico() {}

    public Servico(String nome, int tempoEstimadoMinutos, Double custoTabelado) {
        this.nome = nome;
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
        this.custoTabelado = custoTabelado;
    }

    private Servico(
            Long codigo,
            String nome,
            int tempoEstimadoMinutos,
            Double custoTabelado,
            LocalDateTime criacao,
            LocalDateTime atualizacao,
            long versao) {
        super(codigo, criacao, atualizacao, versao);
        this.nome = nome;
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
        this.custoTabelado = custoTabelado;
    }

    public static Servico reconstituir(
            Long codigo,
            String nome,
            int tempoEstimadoMinutos,
            Double custoTabelado,
            LocalDateTime criacao,
            LocalDateTime atualizacao,
            long versao) {
        return new Servico(
                codigo, nome, tempoEstimadoMinutos, custoTabelado, criacao, atualizacao, versao);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
        registrarAlteracao();
    }

    public int getTempoEstimadoMinutos() {
        return tempoEstimadoMinutos;
    }

    public void setTempoEstimadoMinutos(int tempoEstimadoMinutos) {
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
        registrarAlteracao();
    }

    public Double getCustoTabelado() {
        return custoTabelado;
    }

    public void setCustoTabelado(Double custoTabelado) {
        this.custoTabelado = custoTabelado;
        registrarAlteracao();
    }
}
