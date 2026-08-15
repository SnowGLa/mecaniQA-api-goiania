package com.mecaniqa.api.repository;

import com.mecaniqa.api.model.Peca;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PecaRepository {

    private static PecaRepository instance;

    private final List<Peca> pecas = new ArrayList<>();

    private long proximoCodigo = 1;

    private PecaRepository() {
    }

    public static PecaRepository getInstance() {
        if (instance == null) {
            instance = new PecaRepository();
        }
        return instance;
    }

    public Peca salvar(Peca peca) {
        peca.setCodigo(proximoCodigo);
        proximoCodigo++;
        pecas.add(peca);
        return peca;
    }

    public List<Peca> listarTodas() {
        return pecas;
    }

    public Optional<Peca> buscarPorCodigo(Long codigo) {
        return pecas.stream()
                .filter(p -> p.getCodigo().equals(codigo))
                .findFirst();
    }

    public boolean deletar(Long codigo) {
        return pecas.removeIf(p -> p.getCodigo().equals(codigo));
    }
}