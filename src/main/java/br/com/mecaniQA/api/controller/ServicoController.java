package br.com.mecaniQA.api.controller;

import br.com.mecaniQA.api.model.Servico;
import br.com.mecaniQA.api.repository.ServicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoRepository repository = ServicoRepository.getInstance();

    @PostMapping
    public ResponseEntity<Servico> criar(@RequestBody Servico servico) {
        Servico salvo = repository.salvar(servico);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public ResponseEntity<List<Servico>> listar() {
        return ResponseEntity.ok(repository.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servico> buscarPorId(@PathVariable Long id) {
        Optional<Servico> servico = repository.buscarPorId(id);
        return servico.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servico> atualizar(@PathVariable Long id, @RequestBody Servico dados) {
        Optional<Servico> existente = repository.buscarPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Servico servico = existente.get();
        servico.setTempoEstimadoMinutos(dados.getTempoEstimadoMinutos());
        servico.setCustoTabelado(dados.getCustoTabelado());
        return ResponseEntity.ok(servico);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        boolean removido = repository.deletar(id);
        if (!removido) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }
}
