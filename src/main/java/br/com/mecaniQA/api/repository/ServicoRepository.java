package br.com.mecaniQA.api.repository;

import br.com.mecaniQA.api.model.Servico;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServicoRepository {

    private static ServicoRepository instance;
    private final List<Servico> servicos;

    private ServicoRepository() {
        this.servicos = new ArrayList<>();
    }

    public static ServicoRepository getInstance() {
        if (instance == null) {
            instance = new ServicoRepository();
        }
        return instance;
    }

    public Servico salvar(Servico servico) {
        servicos.add(servico);
        return servico;
    }

    public List<Servico> listarTodos() {
        return servicos;
    }

    public Optional<Servico> buscarPorId(Long id) {
        return servicos.stream()
                .filter(s -> s.getCodigo().equals(id))
                .findFirst();
    }

    public boolean deletar(Long id) {
        return servicos.removeIf(s -> s.getCodigo().equals(id));
    }
}
