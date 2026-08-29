package br.com.mecaniQA.api.repository;

import br.com.mecaniQA.api.model.Peca;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PecaRepository {

    private static PecaRepository instance;
    private final List<Peca> pecas;
    private PecaRepository() {
        this.pecas = new ArrayList<>();
    }

    public static PecaRepository getInstance() {
        if (instance == null) {
            instance = new PecaRepository();
        }
        return instance;
    }

    public Peca salvar(Peca peca) {
        pecas.add(peca);
        return peca;
    }

    public List<Peca> listarTodas() {
        return pecas;
    }

    public Optional<Peca> buscarPorId(Long id) {
        return pecas.stream()
                .filter(p -> p.getCodigo().equals(id))
                .findFirst();
    }

    public boolean deletar(Long id) {
        return pecas.removeIf(p -> p.getCodigo().equals(id));
    }
}
