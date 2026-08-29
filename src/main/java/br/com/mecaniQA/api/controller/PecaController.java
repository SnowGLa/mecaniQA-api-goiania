package br.com.mecaniQA.api.controller;

import br.com.mecaniQA.api.model.Peca;
import br.com.mecaniQA.api.repository.PecaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pecas")
public class PecaController {

    private final PecaRepository repository = PecaRepository.getInstance();

    @PostMapping
    public ResponseEntity<Peca> criar(@RequestBody Peca peca) {
        Peca salva = repository.salvar(peca);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @GetMapping
    public ResponseEntity<List<Peca>> listar() {
        return ResponseEntity.ok(repository.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Peca> buscarPorId(@PathVariable Long id) {
        Optional<Peca> peca = repository.buscarPorId(id);
        return peca.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Peca> atualizar(@PathVariable Long id, @RequestBody Peca dados) {
        Optional<Peca> existente = repository.buscarPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Peca peca = existente.get();
        peca.setPrecoCusto(dados.getPrecoCusto());
        peca.setPrecoVenda(dados.getPrecoVenda());
        peca.setQuantidadeEstoque(dados.getQuantidadeEstoque());
        return ResponseEntity.ok(peca);
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
