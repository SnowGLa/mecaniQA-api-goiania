package com.mecaniqa.api.repository;

import com.mecaniqa.api.model.Servico;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServicoRepository {

    private static ServicoRepository instance;

    private final List<Servico> servicos = new ArrayList<>();

    private long proximoCodigo = 1;

    private ServicoRepository() {
    }

    public static ServicoRepository getInstance() {
        if (instance == null) {
            instance = new ServicoRepository();
        }
        return instance;
    }

    public Servico salvar(Servico servico) {
        servico.setCodigo(proximoCodigo);
        proximoCodigo++;
        servicos.add(servico);
        return servico;
    }

    public List<Servico> listarTodos() {
        return servicos;
    }

    public Optional<Servico> buscarPorCodigo(Long codigo) {
        return servicos.stream()
                .filter(s -> s.getCodigo().equals(codigo))
                .findFirst();
    }

    public boolean deletar(Long codigo) {
        return servicos.removeIf(s -> s.getCodigo().equals(codigo));
    }
}