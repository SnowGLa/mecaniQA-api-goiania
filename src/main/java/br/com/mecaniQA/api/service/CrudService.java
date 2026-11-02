package br.com.mecaniQA.api.service;

import br.com.mecaniQA.api.exception.EntidadeNaoEncontradaException;
import br.com.mecaniQA.api.repository.Repository;

import java.util.List;

public abstract class CrudService<T> {
    protected final Repository<T, Long> repository;

    protected CrudService(Repository<T, Long> repository) {
        this.repository = repository;
    }

    public T buscar(Long codigo) {
        if (codigo == null || codigo <= 0) {
            throw new EntidadeNaoEncontradaException("Registro nao encontrado.");
        }
        return repository
                .findById(codigo)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Registro nao encontrado."));
    }

    public List<T> listar() {
        return repository.findAll();
    }

    public void excluir(Long codigo) {
        if (codigo == null || codigo <= 0 || !repository.deleteById(codigo)) {
            throw new EntidadeNaoEncontradaException("Registro nao encontrado.");
        }
    }
}
