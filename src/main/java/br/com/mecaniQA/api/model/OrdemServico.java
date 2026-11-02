package br.com.mecaniQA.api.model;

import br.com.mecaniQA.api.exception.DadosInvalidosException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class OrdemServico extends EntidadePersistente {
    private StatusOrdemServico status;
    private final List<Long> codigosServicos;
    private final List<Long> codigosPecas;

    private OrdemServico(Builder builder) {
        status = builder.status;
        codigosServicos = List.copyOf(builder.codigosServicos);
        codigosPecas = List.copyOf(builder.codigosPecas);
    }

    private OrdemServico(
            Long codigo,
            StatusOrdemServico status,
            List<Long> servicos,
            List<Long> pecas,
            LocalDateTime criacao,
            LocalDateTime atualizacao,
            long versao) {
        super(codigo, criacao, atualizacao, versao);
        this.status = Objects.requireNonNull(status);
        this.codigosServicos = List.copyOf(servicos);
        this.codigosPecas = List.copyOf(pecas);
    }

    public static OrdemServico reconstituir(
            Long codigo,
            StatusOrdemServico status,
            List<Long> servicos,
            List<Long> pecas,
            LocalDateTime criacao,
            LocalDateTime atualizacao,
            long versao) {
        return new OrdemServico(codigo, status, servicos, pecas, criacao, atualizacao, versao);
    }

    public static Builder builder() {
        return new Builder();
    }

    public StatusOrdemServico getStatus() {
        return status;
    }

    public List<Long> getCodigosServicos() {
        return codigosServicos;
    }

    public List<Long> getCodigosPecas() {
        return codigosPecas;
    }

    public void alterarStatus(StatusOrdemServico status) {
        this.status = Objects.requireNonNull(status);
        registrarAlteracao();
    }

    public static final class Builder {
        private StatusOrdemServico status = StatusOrdemServico.ABERTO;
        private List<Long> codigosServicos = List.of(), codigosPecas = List.of();

        private Builder() {}

        public Builder status(StatusOrdemServico status) {
            this.status = status;
            return this;
        }

        public Builder codigosServicos(List<Long> codigos) {
            codigosServicos = copiar(codigos);
            return this;
        }

        public Builder codigosPecas(List<Long> codigos) {
            codigosPecas = copiar(codigos);
            return this;
        }

        private List<Long> copiar(List<Long> codigos) {
            return codigos == null ? null : Collections.unmodifiableList(new ArrayList<>(codigos));
        }

        public OrdemServico build() {
            if (status == null
                    || codigosServicos == null
                    || codigosPecas == null
                    || (codigosServicos.isEmpty() && codigosPecas.isEmpty())) {
                throw new DadosInvalidosException(
                        "Informe status e pelo menos um servico ou peca.");
            }
            for (var lista : List.of(codigosServicos, codigosPecas)) {
                var unicos = new HashSet<Long>();
                for (Long codigo : lista) {
                    if (codigo == null || codigo <= 0 || !unicos.add(codigo)) {
                        throw new DadosInvalidosException(
                                "Referencias devem ser positivas, nao nulas e sem repeticoes.");
                    }
                }
            }
            return new OrdemServico(this);
        }
    }
}
