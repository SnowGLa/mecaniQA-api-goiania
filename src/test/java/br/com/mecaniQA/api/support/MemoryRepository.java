package br.com.mecaniQA.api.support;

import br.com.mecaniQA.api.exception.ConflitoDadosException;
import br.com.mecaniQA.api.exception.EntidadeNaoEncontradaException;
import br.com.mecaniQA.api.model.EntidadePersistente;
import br.com.mecaniQA.api.repository.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Double somente de teste; nao simula nem valida SQL/MySQL. */
public class MemoryRepository<T extends EntidadePersistente> implements Repository<T, Long> {
    @FunctionalInterface
    public interface Copiador<T> {
        T copiar(T origem, Long codigo, long versao);
    }

    private final Copiador<T> copiador;
    private final Map<Long, T> registros = new LinkedHashMap<>();
    private long sequencia = 1;
    public RuntimeException falha;

    public MemoryRepository(Copiador<T> copiador) {
        this.copiador = copiador;
    }

    private void verificar() {
        if (falha != null) {
            throw falha;
        }
    }

    @Override
    public synchronized T save(T e) {
        verificar();
        long codigo = sequencia++;
        T salvo = copiador.copiar(e, codigo, 0);
        registros.put(codigo, salvo);
        return copiador.copiar(salvo, codigo, 0);
    }

    @Override
    public synchronized Optional<T> findById(Long id) {
        verificar();
        return Optional.ofNullable(registros.get(id))
                .map(e -> copiador.copiar(e, id, e.getVersao()));
    }

    @Override
    public synchronized List<T> findAll() {
        verificar();
        return registros.values().stream()
                .map(e -> copiador.copiar(e, e.getCodigo(), e.getVersao()))
                .toList();
    }

    @Override
    public synchronized T update(T e) {
        verificar();
        T atual = registros.get(e.getCodigo());
        if (atual == null) {
            throw new EntidadeNaoEncontradaException("Ausente.");
        }
        if (atual.getVersao() != e.getVersao()) {
            throw new ConflitoDadosException("Versao desatualizada.");
        }
        T salvo = copiador.copiar(e, e.getCodigo(), e.getVersao() + 1);
        registros.put(e.getCodigo(), salvo);
        return copiador.copiar(salvo, salvo.getCodigo(), salvo.getVersao());
    }

    @Override
    public synchronized boolean deleteById(Long id) {
        verificar();
        return registros.remove(id) != null;
    }
}
