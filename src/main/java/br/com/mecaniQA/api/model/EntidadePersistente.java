package br.com.mecaniQA.api.model;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class EntidadePersistente {
    private final Long codigo;
    private final long versao;
    protected final LocalDateTime dataCriacao;
    protected LocalDateTime dataUltimaAtualizacao;

    protected EntidadePersistente() {
        codigo = null;
        versao = 0;
        dataCriacao = LocalDateTime.now();
        dataUltimaAtualizacao = dataCriacao;
    }

    protected EntidadePersistente(
            Long codigo, LocalDateTime criacao, LocalDateTime atualizacao, long versao) {
        if (codigo == null || codigo <= 0 || versao < 0) {
            throw new IllegalArgumentException("Identidade persistida invalida.");
        }
        this.codigo = codigo;
        this.versao = versao;
        this.dataCriacao = Objects.requireNonNull(criacao);
        this.dataUltimaAtualizacao = Objects.requireNonNull(atualizacao);
    }

    public Long getCodigo() {
        return codigo;
    }

    public long getVersao() {
        return versao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }

    protected void registrarAlteracao() {
        dataUltimaAtualizacao = LocalDateTime.now();
    }
}
